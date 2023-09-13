package com.ebsco.platform.shared.cmsimport.project.closeout;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.JSONArray;
import org.json.JSONObject;

import com.ebsco.platform.shared.cmsimport.utilities.AppPropertiesUtil;
import com.ebsco.platform.shared.cmsimport.utilities.ContentstackUtil;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ProjectCloseOutHandler {

	private static final String MANAGEMENT_TOKEN = AppPropertiesUtil.getProperty("CONTENTSTACK_MANAGEMENT_TOKEN");

	private static final String API_KEY = AppPropertiesUtil.getProperty("CONTENTSTACK_API_KEY");

	private static final String ARTICLE_WORKFLOW = "Article Workflow";

	private static final String WORKFLOW_STAGE_INACTIVE = "Inactive";

	private static final String PROJECT_WORKFLOW = "Project Workflow";

	private static final String WORKFLOW_STAGE_CLOSED_OUT = "Closedout";

	private static final String PROJECT_ID_STR = "projectId";


	public static void main (String [] args) throws IOException {

		//String projectId = "blt0a8235603387da8e";


		Options options = new Options();


		options.addOption(Option.builder("p").longOpt(PROJECT_ID_STR).hasArg()
				.desc("project id")
				.build());


		CommandLineParser parser = new DefaultParser();


		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			if (!line.hasOption(PROJECT_ID_STR)) {
				System.out.println("You need to supply a project id with the p flag");
			}

			else {		


				try {
					String projectId = line.getOptionValue(PROJECT_ID_STR);
					Date projectDate = getProjectDateByUid (projectId);
					int totalNoOfArticles = getCountOfArticlesTiedToProject(projectId);
					List<String>uidList = getArticleUidsTiedToProject(projectId,totalNoOfArticles); 

					for (String articleUid : uidList) {
						resetArticle(articleUid, projectId, projectDate);
						setWorkflowToInactive(articleUid, "Inactive");
					}

					setProjectStatusToClosedOut(projectId);
					System.out.println("Program completed successfully");
				}

				catch (Exception e) {
					System.out.println("Program had exception " + e.getMessage());
				}


			}
		}
		catch (ParseException exp) {
			System.out.println("Unexpected exception:" + exp.getMessage());
		}		

		/*Task Hours	clear out++
		Project Owner Notes	clear out++
		Art spec	clear out ++
		Assigned to	Change value to "unassigned"++
		Urgent	clear out +
		writer due date	clear out ++ 
		editor due date	clear out ++
		Article work type	clear out
		Current Project	moved current project item down to past project field ++
		Past Projects	append item from current project ++
		Project workflow stage (not field)	change project workflow stage to closed out++
		Article workflow stage (not field)	change workflow stage  to inactive++
		Reused	clear out ++
		Last Updated Date	 added project date here (date stamp)++
		Current update type	move to last update type field ++

		Last Update Type	keep and append ??
		 */


	}



	private static void setProjectStatusToClosedOut(String projectId) throws IOException {
		String projectCloseOutStageUid = ContentstackUtil.getWorkflowStageUid(PROJECT_WORKFLOW, WORKFLOW_STAGE_CLOSED_OUT );
		ContentstackUtil.setWorkflowState(projectId, "project",  projectCloseOutStageUid);
	}



	private static void setWorkflowToInactive(String articleUid, String string) throws IOException {
		String wfStageUid = ContentstackUtil.getWorkflowStageUid(ARTICLE_WORKFLOW, WORKFLOW_STAGE_INACTIVE );	
		ContentstackUtil.setWorkflowState(articleUid, "article", wfStageUid);

	}



	private static Date getProjectDateByUid(String projectId) {
		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();
		//MediaType mediaType = MediaType.parse("application/json");
		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/content_types/project/entries?query={\"uid\":\"" + projectId +"\"}")
				.method("GET", null)
				.addHeader("api_key", API_KEY)
				.addHeader("authorization",MANAGEMENT_TOKEN)
				.addHeader("Content-Type", "application/json")
				.build();
		Response response = null;
		String jsonStr = null;
		try {
			response = client.newCall(request).execute();
			jsonStr = response.body().string();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JSONObject jo = new JSONObject(jsonStr);
		JSONArray entriesArray = jo.getJSONArray("entries");
		for (int j=0; j<entriesArray.length(); j++) {
			JSONObject item = entriesArray.getJSONObject(j);
			if (item.has("start_date")) {
				Date projectDate = Date.valueOf(item.getString("start_date"));
				return projectDate;
			}

		}

		return null;
	}



	private static void resetArticle(String articleUid, String projectId, Date projectDate) {
		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();

		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/content_types/article/entries?query={\"uid\":\"" + articleUid +"\"}")
				.method("GET", null)
				.addHeader("api_key", API_KEY)
				.addHeader("authorization",MANAGEMENT_TOKEN)
				.addHeader("Content-Type", "application/json")
				.build();
		Response response = null;
		String jsonStr = null;
		try {
			response = client.newCall(request).execute();
			jsonStr = response.body().string();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(jsonStr);
		JSONObject jo = new JSONObject(jsonStr);
		JSONArray entriesArray = jo.getJSONArray("entries");
		for (int j=0; j<entriesArray.length(); j++) {
			JSONObject item = entriesArray.getJSONObject(j);

			item.put("reused",false);
			item.put("urgent", false);
			item.put("writer_due_date",JSONObject.NULL);
			item.put("editor_due_date",JSONObject.NULL);
			item.put("task_hours",JSONObject.NULL);
			item.put("po_notes",JSONObject.NULL);
			item.put("art_spec_summary", JSONObject.NULL);
			item.put("assigned_to", "unassigned");
			item.put("current_project",JSONObject.NULL);
			item.put("associated_images", JSONObject.NULL);
			item.put("article_work_type", JSONObject.NULL);
			//append to past projects

			JSONArray pastProjectsArray = new JSONArray();

			if (item.has("past_projects") && !item.isNull("past_projects")) {
				pastProjectsArray = item.getJSONArray("past_projects");
			}

			JSONObject projectObj = new JSONObject();

			projectObj.put("uid", projectId);
			projectObj.put("_content_type_uid", "project");
			pastProjectsArray.put(projectObj);
			item.put("past_projects", pastProjectsArray);



			item.put("last_updated_date", convertUtilDateToSqlDate(projectDate));


			String currentUpdateType = null;

			if (item.has("current_update_type") && !item.isNull("current_update_type")) {
				currentUpdateType = item.getString("current_update_type");
			}

			if (currentUpdateType!=null) {
				JSONArray pastUpdateTypesArray = new JSONArray();

				if (item.has("last_update_type") && !item.isNull("last_update_type")) {
					pastUpdateTypesArray = item.getJSONArray("last_update_type");
				}
				if (currentUpdateType!=null) {
					pastUpdateTypesArray.put(currentUpdateType);
					item.put("last_update_type", pastUpdateTypesArray);
				}
				//append to last_update_type
			}

			item.put("current_update_type", JSONObject.NULL);


			ContentstackUtil.updateArticle(articleUid , item);

		}

	}




	public static int getCountOfArticlesTiedToProject (String projectId) throws IOException {

		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();
		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/content_types/article/entries?count=true&query={\"current_project\":{\"$in_query\":{\"uid\":\"" + projectId + "\"}}}")
				.method("GET", null)
				.addHeader("api_key", API_KEY)
				.addHeader("authorization", MANAGEMENT_TOKEN)
				.addHeader("Content-Type", "application/json")
				.build();
		Response response = client.newCall(request).execute();

		String jsonStr = response.body().string();
		JSONObject jo = new JSONObject(jsonStr);
		return jo.getInt("entries");

	}


	private static List<String> getArticleUidsTiedToProject(String projectId, int totalEntries) throws IOException {

		List<String>uidList = new ArrayList<String>();

		int index = (totalEntries/100)+1;

		for (int i=0;i<index;i++) {
			int skip=i *100;

			OkHttpClient client = new OkHttpClient().newBuilder()
					.build();
			Request request = new Request.Builder()
					.url("https://api.contentstack.io/v3/content_types/article/entries?skip=" +skip+ "&query={\"current_project\":{\"$in_query\":{\"uid\":\"" + projectId + "\"}}}")
					.method("GET", null)
					.addHeader("api_key", API_KEY)
					.addHeader("authorization", MANAGEMENT_TOKEN)
					.addHeader("Content-Type", "application/json")
					.build();
			Response response = client.newCall(request).execute();
			String jsonStr = response.body().string();

			//System.out.println(jsonStr);
			JSONObject jo = new JSONObject(jsonStr);
			JSONArray entriesArray = jo.getJSONArray("entries");
			for (int j=0; j<entriesArray.length(); j++) {
				JSONObject item = entriesArray.getJSONObject(j);
				String uid = item.getString("uid");
				uidList.add(uid);
			}

		}

		return uidList;

	}
	
	private static java.sql.Date convertUtilDateToSqlDate(Date date) {
		if (date==null) {
			return null;
		}
		return new java.sql.Date(date.getTime());
	}
}
