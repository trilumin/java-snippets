public class RestClientExample {
    
    public enum Resources{
   	 ADVERTISERS("advertisers"), 
   	 ADVERTISER("advertiser"),
   	 CAMPAIGN("campaign"),
   	 ADGROUPS("adgroups")
   	 ADGROUP("adgroup");
   	 
   	 
   	 final public String path;
   	 
   	 private Resources(String name) {
   		 this.path = name;
   	 }
    }
    
        
    private final static Logger log = LoggerFactory.getLogger(RestClientExample.class);
    private final String partnerId;
    private final WebResource baseResource;
    private final AuthFilter authFilter;
    private final int tokenTTL;
    private final ClientConfiguration config;
    
    
    public RestClientExampleClient(final Client httpClient, final ClientConfiguration config) {
   	 this.tokenTTL = config.getAuthConfiguration().getTokenExpirationInMinutes();
   	 this.partnerId = config.getPartnerId();
   	 this.baseResource = httpClient.resource(config.getApiBaseUrl());
   	 this.authFilter = new AuthFilter(httpClient, config.getApiBaseUrl(),config.getAuthConfiguration());
   	 this.baseResource.addFilter(authFilter);
   	 this.config = config;
    }
    

    public Advertisers getAdvertisers() {
   	 final String onFailureMessage = "getAdvertisers call failed for partner: " + partnerId;
   	 return catchFailures(
   			 build(Resources.ADVERTISERS.path, partnerId).get(ClientResponse.class),
   			 onFailureMessage).getEntity(Advertisers.class);
    }

    
    public Advertiser createAdvertiser(Advertiser advertiser) {
   	 final String onFailureMessage = "createAdvertiser call failed";
   	 return catchFailures(
   			 build(Resources.ADVERTISER.path).post(ClientResponse.class, advertiser),
   			 onFailureMessage).getEntity(Advertiser.class);
    }

    
    public Advertiser getAdvertiserById(String advertiserId) {
   	 final String onFailureMessage = "getAdvertiser call failed";
   	 return catchFailures(
   			 build(Resources.ADVERTISER.path, AdvertiserId).get(ClientResponse.class),
   			 onFailureMessage).getEntity(Advertiser.class);
    }

    
    public Campaign getCampaignById(String campaignId) {
   	 final String onFailureMessage = "getCampaignById call failed";
   	 return    catchFailures(
   			 build(Resources.CAMPAIGN.path, campaignId).get(ClientResponse.class),
   			 onFailureMessage).getEntity(Campaign.class);
    }

    
    public Campaign updateCampaign(Campaign campaign) {
   	 final String onFailureMessage = "updateCampaign call failed";
   	 return catchFailures(
   			 build(Resources.CAMPAIGN.path).put(ClientResponse.class, campaign),
   			 onFailureMessage).getEntity(Campaign.class);
    }

    
    public Campaign createCampaign(Campaign campaign) {
   	 final String onFailureMessage = "createCampaign call failed";
   	 return catchFailures(
   			 build(Resources.CAMPAIGN.path).post(ClientResponse.class, campaign),
   		 	onFailureMessage).getEntity(Campaign.class);
    }

    
    public AdGroup createAdgroup(AdGroup adgroup) {
   	 final String onFailureMessage = "createAdgroup call failed";
   	 return catchFailures(
   			 build(Resources.ADGROUP.path).post(ClientResponse.class, adgroup),
   			 onFailureMessage).getEntity(AdGroup.class);
    }

    
    public AdGroup updateAdgroup(AdGroup adgroup) {
   	 final String onFailureMessage = "updateAdgroup call failed";
   	 return catchFailures(
   			 build(Resources.ADGROUP.path).put(ClientResponse.class, adgroup),
   			 onFailureMessage).getEntity(AdGroup.class);
    }
    
    
    public AdGroups getAdgroupsByCampaignId(String campaignId) {
   	 final String onFailureMessage = "getAdgroupsByCampaignId call failed";
   	 return catchFailures(
   			 build(Resources.ADGROUPS.path, campaignId).get(ClientResponse.class),
   			 onFailureMessage).getEntity(AdGroups.class);
    }
    
    
    public AdGroup getAdgroup(String adgroupId) {
   	 final String onFailureMessage = "getAdgroup call failed";
   	 return catchFailures(
   			 build(Resources.ADGROUP.path, adgroupId).get(ClientResponse.class),
   			 onFailureMessage).getEntity(AdGroup.class);

    }
  
    
    private WebResource.Builder build(String path) {
   	 return this.baseResource
   			 .path(path)
   			 .type(MediaType.APPLICATION_JSON)
   			 .accept(MediaType.APPLICATION_JSON);
    }
    
    
    private WebResource.Builder build(String path, String id) {
   	 return this.baseResource
   			 .path(path)
   			 .path(id)
   			 .type(MediaType.APPLICATION_JSON)
   			 .accept(MediaType.APPLICATION_JSON);
    }

    
    private ClientResponse catchFailures(ClientResponse cr, String onFailureMessage){
   	 try{
   		 final int status = cr.getStatus();
   		 if(cr.getClientResponseStatus().getFamily() != SUCCESSFUL){
   			 String body = cr.getEntity(String.class);
   			 throw new Exception(onFailureMessage +
   					 ": responseStatus = " + status + " responseBody = " + body);
   		 }
   	 }catch(Exception e) {
   		 log.error(e.getMessage());
   	 }
   	 return cr;
    }
}


