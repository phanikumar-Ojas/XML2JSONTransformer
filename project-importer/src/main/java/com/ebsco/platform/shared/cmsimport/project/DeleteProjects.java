package com.ebsco.platform.shared.cmsimport.project;

import java.io.IOException;
import java.util.List;

import com.ebsco.platform.shared.cmsimport.utilities.ContentstackUtil;

public class DeleteProjects {

	
	public static void  main(String [] args) throws IOException {
		
		String contentType = "project";
		int noOfEntries = ContentstackUtil.getCountOfEntries(contentType);
		
		List<String> uids= ContentstackUtil.getListOfEntryUids(noOfEntries,contentType);
		int count = 0;
		for (String uid : uids) {
			System.out.println(count++);
			ContentstackUtil.deleteEntry(uid,contentType);
		}
	}

}
