package com.stackroute.swisit.crawler.service;

import java.io.File;
/*-------Importing Libraries------*/
import java.io.IOException;
import java.util.*;

import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.couchbase.client.deps.com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.stackroute.swisit.crawler.domain.SearcherResult;
import com.stackroute.swisit.crawler.domain.Term;
import com.stackroute.swisit.crawler.exception.DocumentNotScannedException;
import com.stackroute.swisit.crawler.repository.Neo4jRepository;

/* MasterScannerServiceImpl implements MasterScannerService that receives 
 * the searcher result and iterates through it to scan documents documents of each 
 * link received by passing the links to respective services
 * */
@Service
public class MasterScannerServiceImpl implements MasterScannerService{

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private DOMCreatorService domCreatorService;

	@Autowired
	public void setDomCreatorService(DOMCreatorService domCreatorService) {
		this.domCreatorService = domCreatorService;
	}

	private KeywordScannerService keywordScannerService;

	@Autowired
	public void setKeywordScannerService(KeywordScannerService keywordScannerService) {
		this.keywordScannerService = keywordScannerService;
	}
	@Autowired
	Neo4jRepository neo4jRepository;
	private StructureScannerService structureScannerService;

	@Autowired
	public void setStructureScannerService(StructureScannerService structureScannerService) {
		this.structureScannerService = structureScannerService;
	}

	/* Overriding method to scan for documents from searcher service result 
	 * to parse for keywords and structure and accordingly provide the intensity
	 * arguments- searcher result array of objects
	 * returns- string 
	 * */
	@Override
	public String scanDocument(SearcherResult[] searcherResult) throws JsonParseException, JsonMappingException, IOException {
		logger.info("inside master scandocs"+searcherResult.length);
		try {
			if(searcherResult == null) 
				throw new DocumentNotScannedException("Document scanning failed");
		}catch (DocumentNotScannedException e) {
			// TODO Auto-generated catch block
			return "failed";
		}
		for(SearcherResult sr : searcherResult) {
			logger.info(sr.getLink());
			DOMCreatorServiceImpl domCreatorService = new DOMCreatorServiceImpl();
			Document document=domCreatorService.constructDOM(sr.getLink());

			//neo4j implementation

			/*List<Term> l=neo4jRepository.fetchTerms();
			List<String> result=new ArrayList<String>();

			for(Term t:l){
				result.add(t.getName());
			}*/

			 //Iterating terms.json for terms 
			ObjectMapper objectMapper = new ObjectMapper();
	        File file = new File("./src/main/resources/common/Terms.json");
	        List<LinkedHashMap<String,String>> list= (List<LinkedHashMap<String,String>>) objectMapper.readValue(file, ArrayList.class);
	        List<String> result = new ArrayList<String>();

	        for(int i=0;i<list.size();i++){
	            LinkedHashMap<String, String> hashMap = list.get(i);
	            //System.out.println(hashMap.get("name"));
	            result.add(hashMap.get("name"));

	        }

			KeywordScannerServiceImpl keywordScannerService=new KeywordScannerServiceImpl();
			keywordScannerService.scanDocument(document, result , sr);
		}
		return "sucess";
	}


	/* Local class method to get Terms list from neo4j graph
	 * arguments- no args
	 * returns- list of terms from neo4j
	 * */
	/*public List<Term> getTerms() {
		logger.info("Inside getTerms");
		return neo4jRepository.fetchTerms();
	}*/	

}
