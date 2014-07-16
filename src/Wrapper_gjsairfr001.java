import java.io.IOException;
import java.math.BigDecimal;
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
import com.qunar.qfwrapper.bean.search.RoundTripFlightInfo;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFHttpClient;
import com.qunar.qfwrapper.util.QFPostMethod;
import com.travelco.rdf.infocenter.InfoCenter;

/**
 * 往返
 */
public class Wrapper_gjsairfr001 implements QunarCrawler {
	
	public static final NameValuePair ADULT = new NameValuePair("ADULT", "1");
	public static final NameValuePair tc = new NameValuePair("tc", "1");
	public static final NameValuePair travel_type = new NameValuePair("travel_type", "on");
	public static final NameValuePair acceptTerms = new NameValuePair("acceptTerms", "yes");
	public static final NameValuePair zoneDiscount = new NameValuePair("zoneDiscount", "");
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

		String date_1 = arg0.getDepDate();
		String[] dates_1 = date_1.split("-");
		String depDate = dates_1[1] + "%2F" + dates_1[2] + "%2F" + dates_1[0];
		
		String date_2 = arg0.getRetDate();
		String[] dates_2 = date_2.split("-");
		String retDate = dates_2[1] + "%2F" + dates_2[2] + "%2F" + dates_2[0];
		
		String toAirportIATA = InfoCenter.getCityFromAirportCode(arg0.getArr(),"EN") + "+" +StringUtils.substringBefore(InfoCenter.getAirportNameFromCode(arg0.getArr(), "EN"), " ");
		
		map.put("ADULT", "1");
		map.put("sector1_d", arg0.getArr());
		map.put("sector1_o", "a"+arg0.getDep());
		map.put("sector_1_d", dates_1[2]);
		map.put("sector_1_m", dates_1[1]+dates_1[0]);
		map.put("sector_2_d", dates_2[2]);
		map.put("sector_2_m", dates_2[1]+dates_2[0]);
		map.put("tc", "1");
		map.put("travel_type", "on");
		map.put("acceptTerms", "yes");
		map.put("zoneDiscount", "");
		map.put("fromAirportName", InfoCenter.getCityFromAirportCode(arg0.getDep()));
		map.put("toAirportIATA", toAirportIATA);
		map.put("dateFlightFromInput", depDate);
		map.put("dateFlightToInput", retDate);
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
		String date_1 = arg0.getDepDate();
		String[] dates_1 = date_1.split("-");
		String depDate = dates_1[1] + "%2F" + dates_1[2] + "%2F" + dates_1[0];
		
		String date_2 = arg0.getRetDate();
		String[] dates_2 = date_2.split("-");
		String retDate = dates_2[1] + "%2F" + dates_2[2] + "%2F" + dates_2[0];
		
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
					new NameValuePair("sector_1_d", dates_1[2]),//dd
					new NameValuePair("sector_1_m", dates_1[1]+dates_1[0]),//mmdd
					new NameValuePair("sector_2_d", dates_2[2]),
					new NameValuePair("sector_2_m", dates_2[1]+dates_2[0]),					
					tc,travel_type,acceptTerms,zoneDiscount,
					new NameValuePair("fromAirportName", InfoCenter.getCityFromAirportCode(arg0.getDep())),
					new NameValuePair("toAirportIATA", toAirportIATA),
					new NameValuePair("dateFlightFromInput", depDate),
					new NameValuePair("dateFlightToInput", retDate),
					adultQuantityInput,CHILD,INFANT
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
			List<RoundTripFlightInfo> roundTripFlightList = new ArrayList<RoundTripFlightInfo>();//往返组合
			List<OneWayFlightInfo> outboundFlightList = new ArrayList<OneWayFlightInfo>();
			List<OneWayFlightInfo> returnedFlightList = new ArrayList<OneWayFlightInfo>();//返程列表		
			
			String flightData = StringUtils.substringBetween(html, "FR.flightData = ", ";");
			//
			//json中无数据
			if("".equals(flightData)){
				result.setRet(true);
				result.setStatus(Constants.NO_RESULT);
				return result;
			}
			//System.out.println("---"+flightData);
			JSONObject JSONStr = JSON.parseObject(flightData);
			JSONArray obflJSON = JSON.parseArray(JSONStr.getString(arg1.getDep()+arg1.getArr()));
			JSONArray rtflJSON = JSON.parseArray(JSONStr.getString(arg1.getArr()+arg1.getDep()));
			for(int i=0; i<obflJSON.size(); i++){
				if(obflJSON.getString(i).contains(arg1.getDepDate())){
					JSONArray info_3 = obflJSON.getJSONArray(i);//3个元素
					//JSON数组无数据，表示当前售完或无飞行计划
					if(info_3.get(2)==null){
						break;
					}
					JSONArray info_1 = info_3.getJSONArray(1);//提取第1个元素
					JSONArray info_8 = info_1.getJSONArray(0);
					outboundFlightList.add(setOneWayFlightInfo(arg1, info_8,true));
				}
			}
			
			for(int i=0; i<rtflJSON.size(); i++){
				if(rtflJSON.getString(i).contains(arg1.getRetDate())){
					JSONArray info_3 = rtflJSON.getJSONArray(i);//3个元素
					//JSON数组无数据，表示当前售完或无飞行计划
					if(info_3.get(2)==null){
						break;
					}
					JSONArray info_1 = info_3.getJSONArray(1);//提取第1个元素
					JSONArray info_8 = info_1.getJSONArray(0);
					returnedFlightList.add(setOneWayFlightInfo(arg1, info_8,false));
				}
			}

			//当去程或回程无数据时返回NO_RESULT
			if(outboundFlightList.size()==0||returnedFlightList.size()==0){
				result.setRet(true);
				result.setStatus(Constants.NO_RESULT);
				return result;
			}
			//两层循环，对去程和返程list做笛卡尔积得到组合后的所有往返航程
			for(OneWayFlightInfo obfl:outboundFlightList){
				for(OneWayFlightInfo rtfl:returnedFlightList){
					RoundTripFlightInfo round = new RoundTripFlightInfo();
					round.setInfo(obfl.getInfo());//去程航段信息
					round.setOutboundPrice(obfl.getDetail().getPrice());//去程价格
					round.setReturnedPrice(rtfl.getDetail().getPrice());//返程价格
					FlightDetail detail = new FlightDetail();
					detail = obfl.getDetail();
					detail.setPrice(sum(obfl.getDetail().getPrice(),rtfl.getDetail().getPrice()));//往返总价格					
					detail.setTax(sum(obfl.getDetail().getTax(),rtfl.getDetail().getTax()));
					round.setDetail(detail);				//将设置后的去程信息装入往返中
					round.setRetdepdate(rtfl.getDetail().getDepdate());//返程日期
					round.setRetflightno(rtfl.getDetail().getFlightno());//返程航班号list
					round.setRetinfo(rtfl.getInfo());//返程信息
					roundTripFlightList.add(round);//添加到list
				}
			}	
			
			result.setRet(true);
			result.setStatus(Constants.SUCCESS);
			result.setData(roundTripFlightList);
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
	private OneWayFlightInfo setOneWayFlightInfo(FlightSearchParam arg1, JSONArray data, boolean flag) throws Exception {
		OneWayFlightInfo flight = new OneWayFlightInfo();
		List<FlightSegement> segs = new ArrayList<FlightSegement>();
		FlightDetail flightDetail = new FlightDetail();
		List<String> flightNoList = new ArrayList<String>();

			flightNoList.add(data.getString(2));
			flightDetail.setWrapperid(arg1.getWrapperid());
			if(flag){
				flightDetail.setArrcity(arg1.getArr());
				flightDetail.setDepcity(arg1.getDep());
			}else{
				flightDetail.setArrcity(arg1.getDep());
				flightDetail.setDepcity(arg1.getArr());
			}			
			flightDetail.setDepdate(new SimpleDateFormat("yyyy-MM-dd")
					.parse(data.getJSONArray(3).getJSONArray(0).getString(0)));
			flightDetail.setFlightno(flightNoList);
			flightDetail.setMonetaryunit("EUR");
			flightDetail.setPrice(data.getJSONObject(4).getJSONArray("ADT").getJSONObject(1).getDouble("FarePrice"));
			flightDetail.setTax(data.getJSONObject(4).getJSONArray("ADT").getJSONObject(1).getDouble("Tax"));

			FlightSegement seg = new FlightSegement();
			seg.setDeptime(data.getJSONArray(3).getJSONArray(0).getString(1));
			if(flag){
				seg.setDepairport(arg1.getDep());
			}else{
				seg.setDepairport(arg1.getArr());
			}			
			seg.setArrtime(data.getJSONArray(3).getJSONArray(1).getString(1));
			if(flag){
				seg.setArrairport(arg1.getArr());
			}else{
				seg.setArrairport(arg1.getDep());
			}			
			seg.setDepDate(data.getJSONArray(3).getJSONArray(0).getString(0));
			seg.setFlightno(data.getString(2));
			seg.setArrDate(data.getJSONArray(3).getJSONArray(1).getString(0));
			segs.add(seg);
			flight.setDetail(flightDetail);
			flight.setInfo(segs);		
		return flight;
	}
	
	//计算相加，避免丢失精度
	private double sum(double d1,double d2){
		BigDecimal bd1 = new BigDecimal(Double.toString(d1));
        BigDecimal bd2 = new BigDecimal(Double.toString(d2));
        return bd1.add(bd2).doubleValue(); 
	}
}
