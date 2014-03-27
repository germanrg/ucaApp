package com.ucaapp;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

public class GetDistanceFromGoogleMaps extends AsyncTask <Void, Void, String> 
{
	public LatLng dep, arr;
	public String mode;
	public FullscreenActivity main;
	public String distanceText, durationText;
	public int durationValue, distanceValue;

	
	public GetDistanceFromGoogleMaps(FullscreenActivity f, LatLng departure, LatLng arrival, String mode) {
		this.main = f;
		this.dep = departure;
		this.arr = arrival;
		
		this.distanceText = this.durationText = "";
		this.durationValue = this.distanceValue = 0;
		
		if(!mode.equals("walking"))
			this.mode = "driving";
		else this.mode = mode;
	}

	@Override
	protected String doInBackground (Void ...params)
	{
		HttpClient httpClient = new DefaultHttpClient();
		HttpContext localContext = new BasicHttpContext();
		HttpGet httpGet = new HttpGet("http://maps.googleapis.com/maps/api/directions/xml?origin=" 
				+ dep.latitude + "," + dep.longitude
				+ "&destination=" 
				+ arr.latitude + "," + arr.longitude
				+ "&sensor=false&units=metric&mode=" + mode);
		String text = null;
		try{
			HttpResponse response = httpClient.execute(httpGet, localContext);
			HttpEntity entity = response.getEntity();
			text = EntityUtils.toString(entity);
		} 
		catch (Exception e) { 
			System.err.println(e.toString());
			return e.toString();
		}

		return parseXmlResponse(text); 
	}

	protected void onPostExecute(String results){
		main.response(results);
	}//onPOstExecute
	
    private String parseXmlResponse(String text){
    	// Parse to XML
    	try{
    		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    		DocumentBuilder db = factory.newDocumentBuilder();
    		InputSource inStream = new InputSource();
    		inStream.setCharacterStream(new StringReader(text));
    		Document doc = db.parse(inStream);
    		
    		// Get duration text
    		NodeList durationNodeList = doc.getElementsByTagName("duration");
            Node durationNode = durationNodeList.item(durationNodeList.getLength()-1);
            NodeList durationChildsNodeList = durationNode.getChildNodes();
            Node myDurationNode = durationChildsNodeList.item(getNodeIndex(durationChildsNodeList, "text"));
            durationText = myDurationNode.getTextContent();
    
    		// Get duration value
            myDurationNode = durationChildsNodeList.item(getNodeIndex(durationChildsNodeList, "value"));
            durationValue = Integer.parseInt(myDurationNode.getTextContent());
        
            // Get distance text
            NodeList distanceNodeList = doc.getElementsByTagName("distance");
            Node distanceNode = distanceNodeList.item(distanceNodeList.getLength()-1);
            NodeList distanceChildsNodeList = distanceNode.getChildNodes();
            Node myDistanceNode = distanceChildsNodeList.item(getNodeIndex(distanceChildsNodeList, "text"));
            distanceText = myDistanceNode.getTextContent();
            
            // Get distance value
            myDistanceNode = distanceChildsNodeList.item(getNodeIndex(distanceChildsNodeList, "value"));
            distanceValue = Integer.parseInt(myDistanceNode.getTextContent());
        
      	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
    	
    	return distanceText + "\n" + durationText;
    }
    
    private int getNodeIndex(NodeList nodeList, String nodeName) {
        for(int i = 0 ; i < nodeList.getLength() ; i++) {
            if(nodeList.item(i).getNodeName().equals(nodeName))
                return i;
        }
        return -1;
    }

}
