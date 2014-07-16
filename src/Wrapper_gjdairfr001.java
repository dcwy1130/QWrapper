import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.FlightDetail;
import com.qunar.qfwrapper.bean.search.FlightSearchParam;
import com.qunar.qfwrapper.bean.search.FlightSegement;
import com.qunar.qfwrapper.bean.search.OneWayFlightInfo;
import com.qunar.qfwrapper.bean.search.ProcessResultInfo;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFHttpClient;
import com.qunar.qfwrapper.util.QFPostMethod;
import com.travelco.rdf.infocenter.InfoCenter;

/**
 * 单程
 */
public class Wrapper_gjdairfr001 implements QunarCrawler {
	
	public static final NameValuePair ADULT = new NameValuePair("ADULT", "1");
	public static final NameValuePair sector_2_d = new NameValuePair("sector_2_d", "00");
	public static final NameValuePair sector_2_m = new NameValuePair("sector_2_m", "--");
	public static final NameValuePair tc = new NameValuePair("tc", "1");
	public static final NameValuePair travel_type = new NameValuePair("travel_type", "on");
	public static final NameValuePair acceptTerms = new NameValuePair("acceptTerms", "yes");
	public static final NameValuePair zoneDiscount = new NameValuePair("zoneDiscount", "");
	public static final NameValuePair dateFlightToInput = new NameValuePair("dateFlightToInput", "");
	public static final NameValuePair adultQuantityInput = new NameValuePair("adultQuantityInput", "More");
	public static final NameValuePair CHILD = new NameValuePair("CHILD", "0");
	public static final NameValuePair INFANT = new NameValuePair("INFANT", "0");
	
	private static Cookie[] cookies;
	
	@Override
	public BookingResult getBookingInfo(FlightSearchParam arg0) {
		String bookingUrlPre = "https://www.bookryanair.com/SkySales/Booking.aspx";
		BookingResult bookingResult = new BookingResult();

		BookingInfo bookingInfo = new BookingInfo();
		bookingInfo.setAction(bookingUrlPre);
		bookingInfo.setMethod("post");
		Map<String, String> map = new LinkedHashMap<String, String>();

		String date = arg0.getDepDate();
		String[] dates = date.split("-");
		String depDate = dates[1] + "%2F" + dates[2] + "%2F" + dates[0];
		String toAirportIATA = InfoCenter.getCityFromAirportCode(arg0.getArr(),"EN") + "+" +StringUtils.substringBefore(InfoCenter.getAirportNameFromCode(arg0.getArr(), "EN"), " ");
		
		map.put("ADULT", "1");
		map.put("sector1_d", arg0.getArr());
		map.put("sector1_o", "a"+arg0.getDep());
		map.put("sector_1_d", dates[2]);
		map.put("sector_1_m", dates[1]+dates[0]);
		map.put("sector_2_d", "00");
		map.put("sector_2_m", "--");
		map.put("tc", "1");
		map.put("travel_type", "on");
		map.put("acceptTerms", "yes");
		map.put("zoneDiscount", "");
		map.put("fromAirportName", InfoCenter.getCityFromAirportCode(arg0.getDep()));
		map.put("toAirportIATA", toAirportIATA);
		map.put("dateFlightFromInput", depDate);
		map.put("dateFlightToInput", "");
		map.put("adultQuantityInput", "More");
		map.put("CHILD", "0");
		map.put("INFANT", "0");

		bookingInfo.setContentType("UTF-8");
		bookingInfo.setInputs(map);
		bookingResult.setData(bookingInfo);
		bookingResult.setRet(true);
		return bookingResult;
	}

	@Override
	public String getHtml(FlightSearchParam arg0) {
		QFPostMethod post = null;
		// 请求日期格式：06%2F28%2F2014
		String date = arg0.getDepDate();
		String[] dates = date.split("-");
		String depDate = dates[1] + "%2F" + dates[2] + "%2F" + dates[0];
		String toAirportIATA = InfoCenter.getCityFromAirportCode(arg0.getArr(),"EN") + "+" +StringUtils.substringBefore(InfoCenter.getAirportNameFromCode(arg0.getArr(), "EN"), " ");
		//London+Stansted
		try {
			QFHttpClient httpClient = new QFHttpClient(arg0, false);
			httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

			post = new QFPostMethod(
					"https://www.bookryanair.com/SkySales/Booking.aspx");
			
			NameValuePair[] names = { ADULT,
					new NameValuePair("sector1_d", arg0.getArr()),
					new NameValuePair("sector1_o", "a"+arg0.getDep()),
					new NameValuePair("sector_1_d", dates[2]),//dd
					new NameValuePair("sector_1_m", dates[1]+dates[0]),//mmdd
					sector_2_d,sector_2_m,tc,travel_type,acceptTerms,zoneDiscount,
					new NameValuePair("fromAirportName", InfoCenter.getCityFromAirportCode(arg0.getDep())),
					new NameValuePair("toAirportIATA", toAirportIATA),
					new NameValuePair("dateFlightFromInput", depDate),
					dateFlightToInput,adultQuantityInput,CHILD,INFANT
			};
			post.setRequestBody(names);
			post.setRequestHeader("Referer", "http://www.ryanair.com/");
			httpClient.executeMethod(post);
			cookies = httpClient.getState().getCookies();			
			return getFlightDetail();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (post != null) {
				post.releaseConnection();
			}
		}
		return "Exception";
	}

	@Override
	public ProcessResultInfo process(String arg0, FlightSearchParam arg1) {
		String html = arg0;

		ProcessResultInfo result = new ProcessResultInfo();

		if ("Exception".equals(html)) {
			result.setRet(false);
			result.setStatus(Constants.CONNECTION_FAIL);
			return result;
		}		
		
		if (html.contains("Internal server error")) {
			result.setRet(false);
			result.setStatus(Constants.INVALID_AIRLINE);
			return result;
		}	
		
		try {
			List<OneWayFlightInfo> flightList = new ArrayList<OneWayFlightInfo>();
			String flightData = StringUtils.substringBetween(html, "FR.flightData = ", ";");
			//json中无数据
			if("".equals(flightData)){
				result.setRet(true);
				result.setStatus(Constants.NO_RESULT);
				return result;
			}
			
			JSONObject JSONStr = JSON.parseObject(flightData);
			JSONArray jsonArr = JSON.parseArray(JSONStr.getString(arg1.getDep()+arg1.getArr()));
			for(int i=0; i<jsonArr.size(); i++){
				if(jsonArr.getString(i).contains(arg1.getDepDate())){
					JSONArray info_3 = jsonArr.getJSONArray(i);//3个元素
					//JSON数组无数据，表示当前售完或无飞行计划
					if(info_3.get(2)==null){
						result.setRet(true);
						result.setStatus(Constants.NO_RESULT);
						return result;
					}
					JSONArray info_1 = info_3.getJSONArray(1);//提取第1个元素
					JSONArray info_8 = info_1.getJSONArray(0);
					flightList.add(setOneWayFlightInfo(arg1, info_8));
				}
			}			
			
			result.setRet(true);
			result.setStatus(Constants.SUCCESS);
			result.setData(flightList);
			return result;

		} catch (Exception e) {
			e.printStackTrace();
			result.setRet(false);
			result.setStatus(Constants.PARSING_FAIL);
			return result;
		}
	}

	// 获取查询页面后加载的机票信息 GET请求
	private static String getFlightDetail() throws Exception {
		HttpClient httpClient = new HttpClient();
		httpClient.getState().addCookies(cookies);
		GetMethod get = new GetMethod("https://www.bookryanair.com/SkySales/Search.aspx");
		httpClient.executeMethod(get);		
		return get.getResponseBodyAsString();
	}

	//将获取的json数据解析装载
	private OneWayFlightInfo setOneWayFlightInfo(FlightSearchParam arg1, JSONArray data) throws Exception {
		OneWayFlightInfo flight = new OneWayFlightInfo();
		List<FlightSegement> segs = new ArrayList<FlightSegement>();
		FlightDetail flightDetail = new FlightDetail();
		List<String> flightNoList = new ArrayList<String>();

			flightNoList.add(data.getString(2));
			flightDetail.setWrapperid(arg1.getWrapperid());
			flightDetail.setArrcity(arg1.getArr());
			flightDetail.setDepcity(arg1.getDep());
			flightDetail.setDepdate(new SimpleDateFormat("yyyy-MM-dd")
					.parse(data.getJSONArray(3).getJSONArray(0).getString(0)));
			flightDetail.setFlightno(flightNoList);
			flightDetail.setMonetaryunit("EUR");
			flightDetail.setPrice(data.getJSONObject(4).getJSONArray("ADT").getJSONObject(1).getDouble("FarePrice"));
			flightDetail.setTax(data.getJSONObject(4).getJSONArray("ADT").getJSONObject(1).getDouble("Tax"));

			FlightSegement seg = new FlightSegement();
			seg.setDeptime(data.getJSONArray(3).getJSONArray(0).getString(1));
			seg.setDepairport(arg1.getDep());
			seg.setArrtime(data.getJSONArray(3).getJSONArray(1).getString(1));
			seg.setArrairport(arg1.getArr());
			seg.setDepDate(data.getJSONArray(3).getJSONArray(0).getString(0));
			seg.setFlightno(data.getString(2));
			seg.setArrDate(data.getJSONArray(3).getJSONArray(1).getString(0));
			segs.add(seg);
			flight.setDetail(flightDetail);
			flight.setInfo(segs);		
		return flight;
	}
}
