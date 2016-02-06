package pack;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.servlet.ServletUtilities;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Servlet implementation class Graph
 */
@WebServlet("/Graph")
public class Graph extends HttpServlet {
	private static final long serialVersionUID = 1L;
	String name;
	private int pattern;

	/**
	 * mainメソッド
	 *
	 * @param args
	 */
	public static void main(String[] args){
		System.out.println("mainが呼ばれたよ");
		new Graph();
	}

	/**
	 * @throws IOException
	 * @throws ServletException
	 * @see HttpServlet#HttpServlet()
	 */
	public Graph() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			// DBとの接続
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = DriverManager.getConnection(
					"jdbc:mysql://10.33.246.161/tempra","raspi2","VSOLuser123");

			// DBから部屋名、日時、温度、気圧データを取得
			Statement st = conn.createStatement();
			st.executeUpdate("delete from bmp180 "
					+ "where daytime < (now() - interval 1 year)");
			String roomName = "raspi2";		// raspi2の部屋名

			// リクエストパラメータ（グラフのパターン番号）を取得
			request.setCharacterEncoding("UTF-8");
			String strPattern = request.getParameter("pattern");
			int pattern = Integer.parseInt(strPattern);

			// 要求されたグラフに応じたデータを抽出
			String sqlStatement = "SELECT * FROM ";
			switch(pattern){
			case 0:		// raspi2 日
				sqlStatement += "(SELECT room, date_format(daytime, '%H時') AS daytime, temp, press " +
						"FROM bmp180 "+
						"WHERE date(now()) = date(daytime) AND room = '"+ roomName+ "' " +
						"ORDER BY daytime DESC ) t ORDER BY daytime";
				break;

			case 1:		// raspi2 週
				sqlStatement += "(SELECT room, date_format(daytime, '%m月%d日') AS daytime, temp, press " +
						"FROM bmp180 "+
						"WHERE daytime > (now() - interval 1 week) " +
						"AND room = '"+ roomName+ "' " + "AND daytime < now() " +
						"GROUP BY date(daytime) ORDER BY daytime desc ) t ORDER BY daytime";
				break;

			case 2:		// raspi2 月
				sqlStatement += "(SELECT room, date_format(daytime, '%d日') AS daytime, temp, press " +
						"FROM bmp180 WHERE month(now()) = month(daytime) " +
						"AND room = '"+ roomName+ "'" +
						"GROUP BY date(daytime) ORDER BY daytime desc ) t ORDER BY daytime";
				break;

				// TODO: raspi4にも対応できるように…
			case 3:		// raspi4 日（予定）
				sqlStatement += "(SELECT room, date_format(daytime, '%H時') AS daytime, temp, press " +
						"FROM bmp180 "+
						"WHERE date(now()) = date(daytime) AND room = '"+ "raspi4"+ "' " +
						"ORDER BY daytime DESC ) t ORDER BY daytime";
				break;

			case 4:		// raspi4 週
				sqlStatement += "(SELECT room, date_format(daytime, '%m月%d日') AS daytime, temp, press " +
						"FROM bmp180 "+
						"WHERE daytime > (now() - interval 1 week) " +
						"AND room = '"+ roomName+ "' " + "AND daytime < now() " +
						"GROUP BY date(daytime) ORDER BY daytime desc ) t ORDER BY daytime";
				break;

			case 5:		// raspi2 月
				sqlStatement += "(SELECT room, date_format(daytime, '%d日') AS daytime, temp, press " +
						"FROM bmp180 WHERE month(now()) = month(daytime) " +
						"AND room = '"+ roomName+ "'" +
						"GROUP BY date(daytime) ORDER BY daytime desc ) t ORDER BY daytime";
				break;
			}

			ResultSet rs = st.executeQuery(sqlStatement);

			//文字化け防止
			ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());

			// 折れ線グラフの各データ格納用
			DefaultCategoryDataset TempData = new DefaultCategoryDataset();
			DefaultCategoryDataset PressData = new DefaultCategoryDataset();

			// 取得したデータをグラフ用データに格納
			while(rs.next()) {
				String daytime = rs.getString(2);
				int temp = rs.getInt(3);
				int press = rs.getInt(4);
				TempData.addValue(temp, "室温", daytime);
				PressData.addValue(press, "気圧", daytime);
			}

			// 折れ線グラフ作成
			JFreeChart chart = createGraph(TempData, PressData);

			HttpSession session = request.getSession();
//			session.setAttribute("graph", chart);
			String savedfilename = ServletUtilities.saveChartAsPNG(chart, 1200, 350, session);
			session.setAttribute("filename", savedfilename);
			System.out.println(savedfilename);

	        // disp.jspへフォワード
	        RequestDispatcher dispatcher = request.getRequestDispatcher("/disp.jsp");
			dispatcher.forward(request, response);
		} catch (ClassNotFoundException e) {
			System.out.println("ドライバを読み込めませんでした "+ e);
		} catch (SQLException e) {
			System.out.println("データベース接続エラー"+ e);
		}
	}

	/**
	 * 温度データ、気圧データを元にグラフを生成する
	 *
	 * @param tData	温度のデータセット
	 * @param pData	気圧のデータセット
	 * @return		折れ線グラフ
	 */
	private JFreeChart createGraph(DefaultCategoryDataset tData, DefaultCategoryDataset pData) {
		//現在時間取得
		Calendar c = Calendar.getInstance();

		//グラフタイトルのフォーマットを指定
		SimpleDateFormat sdf = new SimpleDateFormat("y年M月");

		// 折れ線グラフ作成（温度軸）
		JFreeChart chart = ChartFactory.createLineChart(
				sdf.format(c.getTime()),
				"時間",
				"温度 [℃]",
				tData,
				PlotOrientation.VERTICAL,
				true,
				false,
				false);

		// 気圧のデータセットを折れ線グラフに追加
		CategoryPlot plot = chart.getCategoryPlot();
		NumberAxis numberAxis = (NumberAxis)plot.getRangeAxis();
		numberAxis.setRange(0, 40);
		plot.setDataset(1, pData);
		plot.mapDatasetToRangeAxis(1, 1);

		// 気圧軸を折れ線グラフに設定
		ValueAxis axis2 = new NumberAxis("気圧 [hPa]");
		axis2.setRange(900, 1100);
		plot.setRangeAxis(1, axis2);

		// 折れ線グラフの表示設定
		LineAndShapeRenderer renderer = new LineAndShapeRenderer();
		plot.setRenderer(1, renderer);
		plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

		return (chart);
	}

	/**
	 * 名前の取得
	 *
	 * @return 名前
	 */
	public String getName(){
		return (this.name);
	}

	public void setName(String name){
		this.name = name;
	}

	/**
	 * グラフのパターン番号を設定する
	 *
	 * @param pattern パターン番号
	 */
	public void setPattern(int pattern){
		this.pattern = pattern;
	}

	/**
	 * グラフのパターン番号を取得する
	 *
	 * @return パターン番号
	 */
	public int getPattern(){
		return (this.pattern);
	}
}
