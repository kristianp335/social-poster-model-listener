package com.liferay.kris.social.poster;

import com.liferay.journal.model.JournalArticle;
import com.liferay.oauth.client.LocalOAuthClient;
import com.liferay.oauth2.provider.model.OAuth2Application;
import com.liferay.oauth2.provider.service.OAuth2ApplicationLocalService;
import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.DocumentException;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.kernel.xml.SAXReader;
import com.liferay.portal.kernel.xml.SAXReaderUtil;
import com.liferay.portal.kernel.xml.XPath;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Random;




import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;



/**
 * @author kpatefield
 */
@Component(
	immediate = true,	
	configurationPid = SocialPosterModelListenerConfiguration.PID,	
	service = ModelListener.class
)
public class SocialPosterModelListener extends BaseModelListener<JournalArticle> {
	
	 private static final Log log = LogFactoryUtil.getLog(
			 SocialPosterModelListener.class
	  );	 
	
	 private volatile SocialPosterModelListenerConfiguration _config;
	 //very specific constants linked to a campaign management use case
	 
	 private String campaignShortName;	 
	 private String campaignName;
	 private String campaignManagerEndpoint;
	 final String utmMedium = "social";
	 final String utmSource = "fb";
	 private String facebookPageId; 
	 private String campaignInstance;
	 private String shortUrl;
	 private String hubspotToken;
	 private Locale locale = LocaleUtil.getDefault();	 
	 private String postBrief;
	 private String postLink;
	 private long postLinkId;
	 private Layout layout;
	 private String layoutUuid;
	 private long layoutGroupId;
	 private boolean privateLayout;
	 private String friendlyUrl;
	 private String facebookIdsField;
	 private long userId;
	 private String applicationErc;
	 
	 
	 @Reference	 
	 OAuth2ApplicationLocalService oAuth2ApplicationLocalService;
	 
	 
	 @Reference
	 LayoutLocalService layoutLocalService;
	 
	 
	 
	 
	 
	 private void getContentFields(JournalArticle journalArticle) throws DocumentException, PortalException {
		 SAXReader reader = SAXReaderUtil.getSAXReader();
		 Document document = reader.read(journalArticle.getContentByLocale(Locale.getDefault().toString()));
		 System.out.println(journalArticle.getContentByLocale(Locale.getDefault().toString()));
		 
		 int leftLimit = 97; // letter 'a'
		 int rightLimit = 122; // letter 'z'
		 int targetStringLength = 10;
	     Random random = new Random();
	     StringBuilder buffer = new StringBuilder(targetStringLength);
	     for (int i = 0; i < targetStringLength; i++) {
		     int randomLimitedInt = leftLimit + (int)(random.nextFloat() * (rightLimit - leftLimit + 1));
		     buffer.append((char) randomLimitedInt);
	     }
	    String generatedString = buffer.toString();

		System.out.println("this is a generated string " + generatedString);
		campaignShortName = _config.campaignShortName();
		campaignName = campaignShortName + " (" + generatedString + ")";
		System.out.println("Campain name is " + campaignName);
		 
		 
		 XPath xPathBrief = SAXReaderUtil.createXPath(
	                "//dynamic-element[@field-reference='" + _config.contentFieldId()
	                        + "']/dynamic-content[@language-id='" + locale.toString() + "']");
		 
		 Element postBriefElement = (Element) xPathBrief.selectSingleNode(document);
	     setPostBrief(postBriefElement.getText());
	     
	     
	     
	     XPath xPathLinkId = SAXReaderUtil.createXPath(
	                "//dynamic-element[@field-reference='" + _config.pageLinkId()
	                        + "']/dynamic-content[@language-id='" + locale.toString() + "']");
	     
	     Element postBriefIdJson = (Element) xPathLinkId.selectSingleNode(document);
	     log.info(postBriefIdJson.getText());
	     JSONObject jsonObject;
	     try {
	    	 jsonObject = JSONFactoryUtil.createJSONObject(postBriefIdJson.getText());
	    	 postLinkId = Long.valueOf((String) jsonObject.get("layoutId"));
	    	 layoutUuid = (String) jsonObject.get("id");
	    	 privateLayout = (boolean) jsonObject.get("privateLayout");
	    	 layoutGroupId = Long.valueOf(jsonObject.get("groupId").toString());
	    	 log.info(layoutUuid);
	    	 log.info(privateLayout);
	    	 log.info(layoutGroupId);
	    	 
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	     
	    Long companyId = PortalUtil.getDefaultCompanyId();
	    layout = layoutLocalService.fetchLayout(layoutUuid, layoutGroupId, privateLayout);	    
	    setFriendlyUrl(layout.getFriendlyURL(locale));
	    
	    //Code to get bearer token for pulse
	    userId = journalArticle.getUserId();
	    
	    
	    JSONObject campaignJSON = JSONFactoryUtil.createJSONObject();
	    campaignJSON.put("name", campaignName);
	    campaignJSON.put("utmMedium", utmMedium);
	    campaignJSON.put("targetUrl", friendlyUrl);
	    campaignJSON.put("utmSource", utmSource);
	    campaignJSON.put("campaignStatus", "active");
	    
	    applicationErc = _config.applicationErc();
	    campaignManagerEndpoint = _config.campaignManagerEndpoint();
	    
	    System.out.println("Code gets this far before the portal catapult");
	    
	    byte[] pulseByte = launch(companyId, Http.Method.POST, applicationErc, campaignJSON, campaignManagerEndpoint, userId);
	    String pulseString = new String(pulseByte, StandardCharsets.UTF_8);
	    JSONObject campaignResponse = JSONFactoryUtil.createJSONObject(pulseString);
	    campaignInstance = _config.campaignInstance();
	    shortUrl = campaignInstance + (String) campaignResponse.get("token");

//	    Client client = ClientBuilder.newClient();
//	    WebTarget target = client.
//	            target(campaignManagerEndpoint);
//	    Response response = target.request().post(Entity.json(campaignJSON.toJSONString()));
//	    String responseString = response.readEntity(String.class);
//	    JSONObject campaignResponse = JSONFactoryUtil.createJSONObject(responseString);
//	    shortUrl = campaignInstance + (String) campaignResponse.get("token");   
//	    response.close();
	    
	    System.out.println("Code gets past the catapult method the short URL is " + shortUrl);
	    	    
	    
	 }

	 @Override
	  public void onAfterUpdate(JournalArticle originalJournalArticle, JournalArticle journalArticle )
	          throws ModelListenerException {
		 
		 
		 System.out.println(String.valueOf(journalArticle.getDDMStructureId()));
		 System.out.println( _config.webContentStructureID());
		 if(String.valueOf(journalArticle.getDDMStructureId()).equals(_config.webContentStructureID())) 
		 {
		
			try {
				getContentFields(journalArticle);
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (PortalException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
			if(_config.activePosting()) {	
				postToFaceBook(journalArticle);
			}
	     }
		 
		
	 }
	 
	 private void postToFaceBook(JournalArticle journalArticle)  {
		    facebookPageId = _config.facebookPageID();
		    Client client = ClientBuilder.newClient();
		    WebTarget target = client.
		            target("https://graph.facebook.com/" + facebookPageId + "/feed");
					
					
		    postBrief = postBrief.replace("<p>", "");
		    postBrief = postBrief.replace("</p>", "");
		    Response response = target.queryParam("access_token", _config.facebookPageAccessToken())
		            .queryParam("message", encode(postBrief + " " + shortUrl))
		            .request()
		            .post(null);
		    postToHubSpot(journalArticle);
		    System.out.println(response.getStatus());
		    System.out.println(response.readEntity(String.class));
		    System.out.println(response.getHeaders().toString());
		    
		    
		 
	 }
	 
	 private void postToHubSpot(JournalArticle journalArticle) {
		 Client client = ClientBuilder.newClient();
		    WebTarget target = client.
		            target("https://api.hubapi.com/marketing-emails/v1/emails");
		    
		    hubspotToken = _config.hubspotToken();
		    Invocation.Builder invocationBuilder = 
		    		target.request(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, hubspotToken);
		    System.out.println(invocationBuilder.toString());
		    
		    
		    JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
		    
		    jsonObject.put("name", campaignShortName);
		    jsonObject.put("subject", campaignShortName);
		    Entity<String> entity = Entity.json(jsonObject.toJSONString());
		    Response response = invocationBuilder.post(entity);
		    System.out.println(response.getStatus());
		    System.out.println(response.readEntity(String.class));
		    System.out.println(response.getHeaders().toString());
		 
	 }
	 
	 public static String encode(String url) {
		    return url.replace(" ", "%20");
		 }

	@Activate
	  @Modified
	  public void activate(Map<String, String> properties) {
	    try {
	      log.info(
	              "Activate Social Media Model Listener"
	      );
	      _config =
	              ConfigurableUtil.createConfigurable(
	                      SocialPosterModelListenerConfiguration.class,
	                      properties
	              );
	      
	    } catch (Exception e) {
	      log.error(
	              "Error while activating Social Poster Model Listener, please provide a valid configuration"
	      );
	      
	    }
	  }

	public String getPostBrief() {
		return postBrief;
	}

	public void setPostBrief(String postBrief) {
		this.postBrief = postBrief;
	}

	public String getPostLink() {
		return postLink;
	}

	public void setPostLink(String postLink) {
		this.postLink = postLink;
	}

	public long getPostLinkId() {
		return postLinkId;
	}

	public void setPostLinkId(long postLinkId) {
		this.postLinkId = postLinkId;
	}

	public String getFriendlyUrl() {
		return friendlyUrl;
	}

	public void setFriendlyUrl(String friendlyUrl) {
		this.friendlyUrl = friendlyUrl;
	}

	public String getFacebookIdsField() {
		return facebookIdsField;
	}

	public void setFacebookIdsField(String facebookIdsField) {
		this.facebookIdsField = facebookIdsField;
	}
	
	public byte[] launch(
			long companyId, Http.Method method,
			String oAuth2ApplicationExternalReferenceCode,
			JSONObject payloadJSONObject, String resourcePath, long userId)
		throws PortalException {

		Http.Options options = new Http.Options();

		options.addHeader(
			HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON);

		if (payloadJSONObject != null) {
			options.setBody(
				payloadJSONObject.toString(), ContentTypes.APPLICATION_JSON,
				StringPool.UTF8);
		}

		OAuth2Application oAuth2Application =
			_oAuth2ApplicationLocalService.
				getOAuth2ApplicationByExternalReferenceCode(
					oAuth2ApplicationExternalReferenceCode, companyId);

		options.setLocation(_getLocation(oAuth2Application, resourcePath));

		options.setMethod(method);

		_localOAuthClient.consumeAccessToken(
			accessToken -> options.addHeader(
				"Authorization", "Bearer " + accessToken),
			oAuth2Application, userId);

		try {
			return _http.URLtoByteArray(options);
		}
		catch (IOException ioException) {
			return ReflectionUtil.throwException(ioException);
		}
	}

	private String _getLocation(
		OAuth2Application oAuth2Application, String resourcePath) {

		if (resourcePath.contains(Http.PROTOCOL_DELIMITER)) {
			return resourcePath;
		}

		String homePageURL = oAuth2Application.getHomePageURL();

		if (homePageURL.endsWith(StringPool.SLASH)) {
			homePageURL = homePageURL.substring(0, homePageURL.length() - 1);
		}

		if (resourcePath.startsWith(StringPool.SLASH)) {
			resourcePath = resourcePath.substring(1);
		}

		return StringBundler.concat(
			homePageURL, StringPool.SLASH, resourcePath);
	}

	@Reference
	private Http _http;

	@Reference
	private LocalOAuthClient _localOAuthClient;

	@Reference
	private OAuth2ApplicationLocalService _oAuth2ApplicationLocalService;

	
}