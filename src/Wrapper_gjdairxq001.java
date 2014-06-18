import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
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
public class Wrapper_gjdairxq001 implements QunarCrawler {
	public static NameValuePair TRIPTYPE = new NameValuePair("TRIPTYPE","O");
	public static NameValuePair RETDEPPORT = new NameValuePair("RETDEPPORT","");
	public static NameValuePair RETARRPORT = new NameValuePair("RETARRPORT","");
	public static final NameValuePair ADULT = new NameValuePair("ADULT","1");
	public static final NameValuePair CHILD = new NameValuePair("CHILD","0");
	public static final NameValuePair INFANT = new NameValuePair("INFANT","0");
	public static final NameValuePair FARETYPE = new NameValuePair("FARETYPE","L");
	public static final NameValuePair source = new NameValuePair("source","btnSearch");
	public static final NameValuePair LANGUAGE = new NameValuePair("LANGUAGE","EN");
	
	private static Cookie[] cookies;

	@Override
	public BookingResult getBookingInfo(FlightSearchParam arg0) {
		String bookingUrlPre = "https://sun.sunexpress.com.tr/web/RezvEntry.xhtml";
		BookingResult bookingResult = new BookingResult();
		
		BookingInfo bookingInfo = new BookingInfo();
		bookingInfo.setAction(bookingUrlPre);
		bookingInfo.setMethod("post");
		Map<String, String> map = new LinkedHashMap<String, String>();
		
		String date = arg0.getDepDate();
		String[] dates = date.split("-");
		String depDate = dates[2]+"%2F"+dates[1]+"%2F"+dates[0];
		
		map.put("TRIPTYPE","O");
		map.put("DEPPORT",arg0.getDep());
		map.put("ARRPORT",arg0.getArr());
		map.put("RETDEPPORT","");
		map.put("RETARRPORT","");
		map.put("from",depDate);
		map.put("DEPDATE",depDate);
		map.put("to",depDate);
		map.put("RETDATE",depDate);
		map.put("ADULT","1");
		map.put("CHILD","0");
		map.put("INFANT","0");
		map.put("FARETYPE","L");
		map.put("source","btnSearch");		
		map.put("LANGUAGE","EN");		   	
		//map.put("Referer", "http://www.sunexpress.com/en");
		
		bookingInfo.setContentType("UTF-8");
		bookingInfo.setInputs(map);		
		bookingResult.setData(bookingInfo);
		bookingResult.setRet(true);
		return bookingResult;
	}

	@Override
	public String getHtml(FlightSearchParam arg0) {
		QFPostMethod post = null;
		//请求日期格式：28%2F06%2F2014
		String date = arg0.getDepDate();
		String[] dates = date.split("-");
		String depDate = dates[2]+"%2F"+dates[1]+"%2F"+dates[0];
		
		try
		{
		QFHttpClient httpClient = new QFHttpClient(arg0, false);
		httpClient.getParams().setCookiePolicy(
				CookiePolicy.BROWSER_COMPATIBILITY);

		post = new QFPostMethod("https://sun.sunexpress.com.tr/web/RezvEntry.xhtml");

			NameValuePair[] names = { TRIPTYPE,
					new NameValuePair("DEPPORT", arg0.getDep()),
					new NameValuePair("ARRPORT", arg0.getArr()), RETDEPPORT,
					RETARRPORT, new NameValuePair("from", depDate),
					new NameValuePair("DEPDATE", depDate),
					new NameValuePair("to", depDate),
					new NameValuePair("RETDATE", depDate), ADULT, CHILD,
					INFANT, FARETYPE, source, LANGUAGE

			};
		post.setRequestBody(names);
		post.setRequestHeader("Referer", "http://www.sunexpress.com/en");
		post.getParams().setContentCharset("UTF-8");
		
		httpClient.executeMethod(post);			
		cookies = httpClient.getState().getCookies();
		return post.getResponseBodyAsString();
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
		
		//无效日期
		if(html.contains("No Flights For Selected Departure Date")){
			result.setRet(false);
			result.setStatus(Constants.INVALID_DATE);
			return result;
		}
		//无效的三字码
		if(html.contains("Unknown Port")){
			result.setRet(false);
			result.setStatus(Constants.INVALID_AIRLINE);
			return result;
		}
		//没有选择的航班到达日期
		if(html.contains("No Flights For Selected Arrival Date")){
			result.setRet(false);
			result.setStatus(Constants.INVALID_DATE);
			return result;
		}		
		try {					
			List<OneWayFlightInfo> flightList = new ArrayList<OneWayFlightInfo>();
			//截取日期，拼装成"27/07/14"格式
			String date = arg1.getDepDate();
			String[] dates = date.split("-");
			String start = dates[2] + "/" + dates[1] + "/" + dates[0].substring(dates[0].length()-2,dates[0].length());
			//String tables = StringUtils.substringBetween(html, start,"/14</td>");
			String tables;
			if(StringUtils.substringAfter(html, start).contains("<table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" class=\"day-listing\">")){
				tables = StringUtils.substringBetween(html, start,"/14</td>");//返程数据				
			}else{
				tables = StringUtils.substringBetween(html, start,"All Your Flight Details");
			}			
			
			int count = StringUtils.countMatches(tables, "</table>");//得到行程列表个数
			if(count == 1){//单个行程
				//是否需要中转
				if(!tables.contains("No Seat")){// No Seat判断是否有座位
					flightList.add(setOneWayFlightInfo(arg1.getWrapperid(), arg1.getDepDate(), tables, tables.contains("via")));
				}								
			} else {
				String[] table = tables.split("</table>");
				for (int i = 0; i < count; i++) {// 多个行程,迭代取值
					if (table[i].contains("No Seat")) {// No Seat判断是否有座位
						continue;
					} else {
						flightList.add(setOneWayFlightInfo(arg1.getWrapperid(),
								arg1.getDepDate(), table[i], tables
										.contains("via")));
					}
				}
			}
			if(flightList.size()==0){
				result.setRet(true);
				result.setStatus(Constants.NO_RESULT);
				return result;
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
	
	//获取查询页面后加载的机票信息
	private static String getFlightDetail(String requestParam) throws Exception{
		HttpClient httpClient = new HttpClient();
		PostMethod post = new PostMethod("https://sun.sunexpress.com.tr/mobileAjaxExecuter");
		NameValuePair[] names = { 
				new NameValuePair("beanName", "rezvResultsBean"),
				new NameValuePair("operation", "selectDepFlight"),
				new NameValuePair("requestParam", requestParam),
		};
		post.setRequestBody(names);
		httpClient.getState().addCookies(cookies);
		
		post.setRequestHeader("Referer", "https://sun.sunexpress.com.tr/web/RezvEntry.xhtml");
		post.getParams().setContentCharset("UTF-8");
		httpClient.executeMethod(post);
		return post.getResponseBodyAsString();
	}
	
	//处理一个table数据（单次行程）
	private OneWayFlightInfo setOneWayFlightInfo(String wrapperid,String depDate,String table,Boolean flag) throws Exception{
		OneWayFlightInfo flight = new OneWayFlightInfo();
		List<FlightSegement> segs = new ArrayList<FlightSegement>();
		FlightDetail flightDetail = new FlightDetail();		
		List<String> flightNoList = new ArrayList<String>();
		
		String input = StringUtils.substringBetween(table, "<input",">");//得到input radio发送post请求的参数值
		String requestParam = StringUtils.substringBetween(input, "(", ")");
		String JSONStr = getFlightDetail(requestParam);
		JSONObject obj = JSON.parseObject(JSONStr);
		//schArrDate  11/08/2014 - 16:25
		String[] arrDates = obj.getString("schArrDate").replaceAll(" ", "").split("-")[0].split("/");
		String arrDate = arrDates[2]+"-"+arrDates[1]+"-"+arrDates[0];
		
		if(flag){//需要中转
			String[] flightNos = (obj.getString("operatingCarrier")+obj.getString("operatingFlightNo")).split("/");
			//String[] flightNos = StringUtils.substringBetween(table, "<td width=\"60\">", "</td>").replaceAll(" ", "").split("/");
			for(int i=0; i<flightNos.length; i++){
				flightNoList.add(flightNos[i]);
			}			
			flightDetail.setWrapperid(wrapperid);
			flightDetail.setArrcity(obj.getString("arrPort"));
			flightDetail.setDepcity(obj.getString("depPort"));
			flightDetail.setDepdate(new SimpleDateFormat("yyyy-MM-dd").parse(depDate));
			flightDetail.setFlightno(flightNoList);
			flightDetail.setMonetaryunit(obj.getString("showCurrency"));
			flightDetail.setPrice(obj.getDoubleValue("totalDepAdultFare"));
			flightDetail.setTax(sum(obj.getDoubleValue("totalTax"),obj.getDoubleValue("totalServiceFee"),obj.getDoubleValue("totalDepSurcharge")));
			//两个tr，包含两上行程列表
			String[] tr = StringUtils.substringBetween(table, "<tr class=\"inform hidden\" rendered=\"currentFlight.flightSegments\">", "</tbody>").split("<tr class=\"inform hidden\" rendered=\"currentFlight.flightSegments\">");
			
			for(int i=0; i<flightNos.length; i++){				
				FlightSegement seg = new FlightSegement();
				String[] times = StringUtils.substringBetween(tr[i], "<td>", "</td>").replaceAll(" ", "").split("-");
				seg.setDeptime(times[0]);
				seg.setDepairport(StringUtils.substringBetween(tr[i], "(", ")").replaceAll(" ", ""));
				seg.setArrtime(times[1]);				
				seg.setArrairport(StringUtils.substringBetween(StringUtils.substringBetween(tr[i], ")", "<br>"), "(", ")").replaceAll(" ", ""));			
				seg.setFlightno(StringUtils.substringBetween(tr[i], "<br>", "</td>").replaceAll("&#9;", "").replaceAll(" ", "").replaceAll("\n", ""));
				seg.setDepDate(depDate);
				seg.setArrDate(arrDate);
				segs.add(seg);
			}
			
			flight.setDetail(flightDetail);
			flight.setInfo(segs);
		}else{//不需要中转
			flightNoList.add(obj.getString("operatingCarrier")+obj.getString("operatingFlightNo"));
			flightDetail.setWrapperid(wrapperid);
			flightDetail.setArrcity(obj.getString("arrPort"));
			flightDetail.setDepcity(obj.getString("depPort"));
			flightDetail.setDepdate(new SimpleDateFormat("yyyy-MM-dd").parse(depDate));
			flightDetail.setFlightno(flightNoList);
			flightDetail.setMonetaryunit(obj.getString("showCurrency"));
			flightDetail.setPrice(obj.getDoubleValue("totalDepAdultFare"));
			flightDetail.setTax(sum(obj.getDoubleValue("totalTax"),obj.getDoubleValue("totalServiceFee"),obj.getDoubleValue("totalDepSurcharge")));
			
			FlightSegement seg = new FlightSegement();
			//截取时间10:21-16:15
			String[] times = StringUtils.substringBetween(table, "<td width=\"90\">", "</td>").replaceAll(" ", "").split("-");
			seg.setDeptime(times[0]);
			seg.setDepairport(obj.getString("depPort"));
			seg.setArrtime(times[1]);
			seg.setArrairport(obj.getString("arrPort"));
			seg.setDepDate(depDate);
			seg.setFlightno(obj.getString("operatingCarrier")+obj.getString("operatingFlightNo"));
			seg.setArrDate(arrDate);			
			segs.add(seg);			
			flight.setDetail(flightDetail);
			flight.setInfo(segs);			
		}
		
		return flight;
	}
	//计算相加，避免丢失精度
	private double sum(double d1,double d2,double d3){
		BigDecimal bd1 = new BigDecimal(Double.toString(d1));
        BigDecimal bd2 = new BigDecimal(Double.toString(d2));
        BigDecimal bd3 = new BigDecimal(Double.toString(d3));
        return bd1.add(bd2).add(bd3).doubleValue(); 
	}

}
