package qunar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
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

/**
 * 往返
 */
public class Wrapper_gjsairxq001 implements QunarCrawler {

	public static NameValuePair TRIPTYPE = new NameValuePair("TRIPTYPE","R");
	public static NameValuePair RETDEPPORT = new NameValuePair("RETDEPPORT","");
	public static NameValuePair RETARRPORT = new NameValuePair("RETARRPORT","");
	public static final NameValuePair ADULT = new NameValuePair("ADULT","1");
	public static final NameValuePair CHILD = new NameValuePair("CHILD","0");
	public static final NameValuePair INFANT = new NameValuePair("INFANT","0");
	public static final NameValuePair FARETYPE = new NameValuePair("FARETYPE","L");
	public static final NameValuePair source = new NameValuePair("source","btnSearch");
	public static final NameValuePair LANGUAGE = new NameValuePair("LANGUAGE","EN");
	
	private static Cookie[] cookies;

	public static void main(String[] args) throws Exception {
		FlightSearchParam fsp = new FlightSearchParam();
		fsp.setDep("FRA");
		fsp.setArr("SAW");
		fsp.setDepDate("2014-08-26");
		fsp.setRetDate("2014-09-06");
		Wrapper_gjsairxq001 wg = new Wrapper_gjsairxq001();
		String html = wg.getHtml(fsp);
		System.out.println(wg.process(html, fsp).getStatus());
		System.out.println(wg.getBookingInfo(fsp).getData().getInputs());
	}
	@Override
	public BookingResult getBookingInfo(FlightSearchParam arg0) {
		String bookingUrlPre = "https://sun.sunexpress.com.tr/web/RezvEntry.xhtml";
		BookingResult bookingResult = new BookingResult();
		
		BookingInfo bookingInfo = new BookingInfo();
		bookingInfo.setAction(bookingUrlPre);
		bookingInfo.setMethod("post");
		Map<String, String> map = new LinkedHashMap<String, String>();
		
		String depDate = arg0.getDepDate();
		String[] depDates = depDate.split("-");
		String newDepDate = depDates[2]+"%2F"+depDates[1]+"%2F"+depDates[0];
		String retDate = arg0.getRetDate();
		String[] retDates = retDate.split("-");
		String newRetDate = retDates[2]+"%2F"+retDates[1]+"%2F"+retDates[0];
		
		map.put("TRIPTYPE","R");
		map.put("DEPPORT",arg0.getDep());
		map.put("ARRPORT",arg0.getArr());
		map.put("RETDEPPORT","");
		map.put("RETARRPORT","");
		map.put("from",newDepDate);
		map.put("DEPDATE",newDepDate);
		map.put("to",newRetDate);
		map.put("RETDATE",newRetDate);
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
		String date1 = arg0.getDepDate();
		String[] dates1 = date1.split("-");
		String depDate = dates1[2]+"%2F"+dates1[1]+"%2F"+dates1[0];
		String date2 = arg0.getRetDate();
		String[] dates2 = date2.split("-");
		String retDate = dates2[2]+"%2F"+dates2[1]+"%2F"+dates2[0];
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
					new NameValuePair("to", retDate),
					new NameValuePair("RETDATE", retDate), ADULT, CHILD,
					INFANT, FARETYPE, source, LANGUAGE

			};
		post.setRequestBody(names);
		post.setRequestHeader("Referer", "http://www.sunexpress.com/");
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
			List<RoundTripFlightInfo> roundTripFlightList = new ArrayList<RoundTripFlightInfo>();//往返组合
			List<OneWayFlightInfo> outboundFlightList = new ArrayList<OneWayFlightInfo>();//去程列表
			List<OneWayFlightInfo> returnedFlightList = new ArrayList<OneWayFlightInfo>();//返程列表
			
			//截取去程日期，拼装成"27/07/14"格式
			String depDate = arg1.getDepDate();
			String[] depDates = depDate.split("-");
			String start = depDates[2] + "/" + depDates[1] + "/" + depDates[0].substring(depDates[0].length()-2,depDates[0].length());
			String depTables = StringUtils.substringBetween(html, start,"/14</td>");//去程数据
			
			//截取返程日期，拼装成"27/07/14"格式
			String retDate = arg1.getRetDate();
			String[] retDdates = retDate.split("-");
			String end = retDdates[2] + "/" + retDdates[1] + "/" + retDdates[0].substring(retDdates[0].length()-2,retDdates[0].length());
			String retTables = StringUtils.substringBetween(html, end,"/14</td>");//返程数据
			
			int depCount = StringUtils.countMatches(depTables, "</table>");//得到去程列表个数
			int retCount = StringUtils.countMatches(retTables, "</table>");//得到返程列表个数
			
			if(depCount == 1){//单个行程
				//是否需要中转
				if(!depTables.contains("No Seat")){//No Seat判断是否有座位
					outboundFlightList.add(setOneWayFlightInfo(arg1.getWrapperid(), arg1.getDepDate(), depTables, depTables.contains("via")));
				}							
			}else{
				String[] table = depTables.split("</table>");
				for(int i = 0 ;i < depCount ; i++){//多个行程,迭代取值
					if(table[i].contains("No Seat")){//No Seat判断是否有座位
						continue;
					}else{
						outboundFlightList.add(setOneWayFlightInfo(arg1.getWrapperid(), arg1.getDepDate(), table[i], depTables.contains("via")));
					}
				}
			}
			
			if(retCount == 1){//单个行程
				//是否需要中转
				if(!retTables.contains("No Seat")){//No Seat判断是否有座位
					returnedFlightList.add(setOneWayFlightInfo(arg1.getWrapperid(), arg1.getRetDate(), retTables, retTables.contains("via")));
				}								
			}else{
				String[] table = retTables.split("</table>");
				for(int i = 0 ;i < retCount ; i++){//多个行程,迭代取值
					if(table[i].contains("No Seat")){//No Seat判断是否有座位
						continue;
					}else{
						returnedFlightList.add(setOneWayFlightInfo(arg1.getWrapperid(), arg1.getRetDate(), table[i], retTables.contains("via")));
					}					
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
					detail.setPrice(obfl.getDetail().getPrice()+rtfl.getDetail().getPrice());//往返总价格
					detail.setTax(obfl.getDetail().getTax()+rtfl.getDetail().getTax());//往返总税费
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
	
	//获取查询页面后加载的机票信息
	private static String getFlightDetail(String operationParam,String requestParam) throws Exception{
		HttpClient httpClient = new HttpClient();
		PostMethod post = new PostMethod("https://sun.sunexpress.com.tr/mobileAjaxExecuter");
		NameValuePair[] names = { 
				new NameValuePair("beanName", "rezvResultsBean"),
				new NameValuePair("operation", operationParam),
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
		String operationParam = StringUtils.substringBetween(input, "onclick=\"", "(");//selectArrFlight  selectDepFlight
		String JSONStr = getFlightDetail(operationParam,requestParam);
		JSONObject obj = JSON.parseObject(JSONStr);
		
		if(flag){//需要中转
					
			if(operationParam.equals("selectDepFlight")){//去程
				String[] flightNos = (obj.getString("operatingCarrier")+obj.getString("operatingFlightNo")).split("/");
				for(int i=0; i<flightNos.length; i++){
					flightNoList.add(flightNos[i]);
				}
								
				flightDetail.setArrcity(obj.getString("arrPort"));
				flightDetail.setDepcity(obj.getString("depPort"));
				flightDetail.setPrice(obj.getDoubleValue("totalDepAdultFare"));
				flightDetail.setTax(obj.getDoubleValue("totalDepTax")+obj.getDoubleValue("totalServiceFee")+obj.getDoubleValue("totalDepSurcharge"));
				
				//两个tr，包含两上行程列表
				String[] tr = StringUtils.substringBetween(table, "<tr class=\"inform hidden\" rendered=\"currentFlight.flightSegments\">", "</tbody>").split("<tr class=\"inform hidden\" rendered=\"currentFlight.flightSegments\">");
				for(int i=0; i<flightNos.length; i++){					
					FlightSegement seg = new FlightSegement();
					String[] times = StringUtils.substringBetween(tr[i], "<td>", "</td>").replaceAll(" ", "").split("-");
					seg.setDeptime(times[0]);
					seg.setDepairport(StringUtils.substringBetween(tr[i], "(", ")").replaceAll(" ", ""));
					seg.setArrtime(times[1]);				
					seg.setArrairport(StringUtils.substringBetween(StringUtils.substringBetween(tr[i], ")", "<br>"), "(", ")").replaceAll(" ", ""));			
					seg.setFlightno(StringUtils.substringBetween(tr[i], "<br>", "</td>").replaceAll("&#9;", "").replaceAll(" ", ""));
					seg.setDepDate(depDate);
					segs.add(seg);
				}
				
			}else{//返程
				String[] flightNos = (obj.getString("retOperatingCarrier")+obj.getString("retOperatingFlightNo")).split("/");
				for(int i=0; i<flightNos.length; i++){
					flightNoList.add(flightNos[i]);
				}
				flightDetail.setArrcity(obj.getString("retArrPort"));
				flightDetail.setDepcity(obj.getString("retDepPort"));
				flightDetail.setPrice(obj.getDoubleValue("totalArrAdultFare"));
				flightDetail.setTax(obj.getDoubleValue("totalArrTax")+obj.getDoubleValue("totalServiceFee")/2+obj.getDoubleValue("totalArrSurcharge"));//返回时，服务费算了往返的，所以此次除2
				
				//两个tr，包含两上行程列表
				String[] tr = StringUtils.substringBetween(table, "<tr class=\"inform hidden\" rendered=\"currentFlight.flightSegments\">", "</tbody>").split("<tr class=\"inform hidden\" rendered=\"currentFlight.flightSegments\">");
				for(int i=0; i<flightNos.length; i++){
					FlightSegement seg = new FlightSegement();
					String[] times = StringUtils.substringBetween(tr[i], "<td>", "</td>").replaceAll(" ", "").split("-");
					seg.setDeptime(times[0]);
					seg.setDepairport(StringUtils.substringBetween(tr[i], "(", ")").replaceAll(" ", ""));
					seg.setArrtime(times[1]);				
					seg.setArrairport(StringUtils.substringBetween(StringUtils.substringBetween(tr[i], ")", "<br>"), "(", ")").replaceAll(" ", ""));			
					seg.setFlightno(StringUtils.substringBetween(tr[i], "<br>", "</td>").replaceAll("&#9;", "").replaceAll(" ", ""));
					seg.setDepDate(depDate);
					segs.add(seg);
				}
			}			
			flightDetail.setWrapperid(wrapperid);			
			flightDetail.setDepdate(new SimpleDateFormat("yyyy-MM-dd").parse(depDate));
			flightDetail.setFlightno(flightNoList);
			flightDetail.setMonetaryunit(obj.getString("showCurrency"));
						
			flight.setDetail(flightDetail);
			flight.setInfo(segs);						
		}else{//不需要中转
			FlightSegement seg = new FlightSegement();			
			if(operationParam.equals("selectDepFlight")){//去程
				flightNoList.add(obj.getString("operatingCarrier")+obj.getString("operatingFlightNo"));
				flightDetail.setArrcity(obj.getString("arrPort"));
				flightDetail.setDepcity(obj.getString("depPort"));
				flightDetail.setPrice(obj.getDoubleValue("totalDepAdultFare"));
				flightDetail.setTax(obj.getDoubleValue("totalDepTax")+obj.getDoubleValue("totalServiceFee")+obj.getDoubleValue("totalDepSurcharge"));
				//截取时间10:21-16:15
				seg.setDeptime(StringUtils.substringAfter(obj.getString("schDepDate"), "-").replace(" ", ""));
				seg.setDepairport(obj.getString("depPort"));
				seg.setArrtime(StringUtils.substringAfter(obj.getString("schArrDate"), "-").replace(" ", ""));
				seg.setArrairport(obj.getString("arrPort"));
				seg.setDepDate(depDate);
				seg.setFlightno(obj.getString("operatingCarrier")+obj.getString("operatingFlightNo"));
				segs.add(seg);			
				
			}else{//返程
				flightNoList.add(obj.getString("retOperatingCarrier")+obj.getString("retOperatingFlightNo"));
				flightDetail.setArrcity(obj.getString("retArrPort"));
				flightDetail.setDepcity(obj.getString("retDepPort"));
				flightDetail.setPrice(obj.getDoubleValue("totalArrAdultFare"));
				flightDetail.setTax(obj.getDoubleValue("totalArrTax")+obj.getDoubleValue("totalServiceFee")/2+obj.getDoubleValue("totalArrSurcharge"));
				//截取时间10:21-16:15
				seg.setDeptime(StringUtils.substringAfter(obj.getString("retSchDepDate"), "-").replace(" ", ""));
				seg.setDepairport(obj.getString("retDepPort"));
				seg.setArrtime(StringUtils.substringAfter(obj.getString("retSchArrDate"), "-").replace(" ", ""));
				seg.setArrairport(obj.getString("retDepPort"));
				seg.setDepDate(depDate);
				seg.setFlightno(obj.getString("retOperatingCarrier")+obj.getString("retOperatingFlightNo"));
				segs.add(seg);
			}			
			flightDetail.setWrapperid(wrapperid);			
			flightDetail.setDepdate(new SimpleDateFormat("yyyy-MM-dd").parse(depDate));
			flightDetail.setFlightno(flightNoList);
			flightDetail.setMonetaryunit(obj.getString("showCurrency"));
						
			flight.setDetail(flightDetail);
			flight.setInfo(segs);			
		}		
		return flight;
	}

}
