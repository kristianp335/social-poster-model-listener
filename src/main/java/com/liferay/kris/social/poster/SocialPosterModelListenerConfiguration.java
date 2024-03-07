package com.liferay.kris.social.poster;

import aQute.bnd.annotation.metatype.Meta;





@Meta.OCD(
        id = SocialPosterModelListenerConfiguration.PID,
        localization = "content/Language",
        name = "social-poster-model-listener"
)
interface SocialPosterModelListenerConfiguration {

    @Meta.AD(
            name = "social-media-posting-active",
            description = "Enable or disable social media posting",
            required = false,
            deflt = "false"
    )
    public boolean activePosting();

    @Meta.AD(
            name = "Web-Content-Structure-ID",
            description = "Web content strcuture to be monitored, clearly you don't want to post everything in web content",
            required = false,
            deflt = "0"
    )
    public String webContentStructureID();

    
    @Meta.AD(
            name = "social-media-posts-ids-field-id",
            description = "Create a string field in your structure to store the id to prevent reposting",
            required = true,
            deflt = "id0123"
    )
    public String SocialMediaPostsIDFieldID();

    @Meta.AD(
            name = "facebook-page-access-token",
            description = "Page access token is required to allow liferay to post a feed on your business page",
            required = true,
            deflt = "0"
    )
    public String facebookPageAccessToken();
    
    @Meta.AD(
            name = "facebook-page-id",
            description = "Page ID is required to allow liferay to post a feed on your business page",
            required = true,
            deflt = "182944204891911"
    )
    public String facebookPageID();
    
    @Meta.AD(
            name = "content-field-id",
            description = "The content field of your structure you wish to post",
            required = true,
            deflt = "0"
    )
    public String contentFieldId();
    
    @Meta.AD(
            name = "pageLinkId",
            description = "The page link field of your structure you wish to post",
            required = true,
            deflt = "0"
    )
    public String pageLinkId();
    
    @Meta.AD(
            name = "campaignManagerEndpoint",
            description = "Pulse Campaign Manager Endpon, should be a fully qualified url",
            required = true,
            deflt = "https://pulse.forrester.liferaycloud.com/api/campaigns"
    )
    public String campaignManagerEndpoint();
    
    @Meta.AD(
            name = "campaignShortName",
            description = "A short name for the campaign",
            required = true,
            deflt = "Mobile Insurance 2023"
    )
    public String campaignShortName();
    
    @Meta.AD(
            name = "campaignInstance",
            description = "General fully qualified URL for the campaign, should be similar to the endpoint",
            required = true,
            deflt = "https://pulse.forrester.liferaycloud.com/"
    )
    public String campaignInstance();
    
    @Meta.AD(
            name = "hubspotToken",
            description = "General fully qualified URL for the campaign, should be similar to the endpoint",
            required = true,
            deflt = "Bearer pat-na1-3bdfeea1-1251-46a5-b8d9-2f5f62500adc"
    )
    public String hubspotToken();
    
    @Meta.AD(
            name = "applicationErc",
            description = "Pulse application ERC",
            required = true,
            deflt = "pulse-micro-service-oauth-application-user-agent"
    )
    public String applicationErc();    


    public static final String PID = "com.liferay.kris.social.poster.SocialMediaModelListenerConfiguration";
    
}