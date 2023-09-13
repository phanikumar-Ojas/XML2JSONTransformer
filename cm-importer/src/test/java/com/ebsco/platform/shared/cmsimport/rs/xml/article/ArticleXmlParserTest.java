package com.ebsco.platform.shared.cmsimport.rs.xml.article;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashSet;

import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.Test;

import com.ebsco.platform.shared.cmsimport.rs.domain.Article;
import com.ebsco.platform.shared.cmsimport.rs.util.TestUtils;

public class ArticleXmlParserTest {
    
    private static final String FOLDER = "/article/";
    
    @Test
    public void poetry_movements_2017_sp_ency_lit_310665() throws JSONException {
        check("poetry_movements_2017_sp_ency_lit_310665.xml");
    }

    @Test
    public void gl20c_rs_31828() throws JSONException {
        check("gl20c_rs_31828.xml");
    }
    
    @Test
    public void rssalemprimaryencyc_20190926_64() throws JSONException {
        check("rssalemprimaryencyc_20190926_64.xml");
    }

    @Test
    public void musc_rs_79605() throws JSONException {
        check("musc_rs_79605.xml");
    }
    
    @Test//audio
    public void rscountries_4143() throws JSONException {
        check("rscountries_4143.xml");
    }
    
    @Test
    public void rsspencyclopedia_20200316_23() throws JSONException {
        check("rsspencyclopedia_20200316_23.xml");
    }
    
    @Test//if (children == null) because span without children
    public void rsdiseases_32035() throws JSONException {
        check("rsdiseases_32035.xml");
    }
    
    @Test//no ref to image file
    public void rssalemprimaryencyc_20170809_126() throws JSONException {
        check("rssalemprimaryencyc_20170809_126.xml");
    }
    
    @Test//1 boxed-text -> blockquote
    public void rsdiseases_118520() throws JSONException {
        check("rsdiseases_118520.xml");
    }
    
    @Test//2
    public void genetics_rs_87954() throws JSONException {
        check("genetics_rs_87954.xml");
    }
    
    @Test//3
    public void ell_2222_rs_156202() throws JSONException {
        check("ell_2222_rs_156202.xml");
    }
    
    @Test//4
    public void rstocmmg7_118454() throws JSONException {
        check("rstocmmg7_118454.xml");
    }
    
    @Test//5
    public void csd_rs_161703() throws JSONException {
        check("csd_rs_161703.xml");
    }
    
    @Test//6
    public void pbh_rs_223020() throws JSONException {
        check("pbh_rs_223020.xml");
    }
    
    @Test//7
    public void _1990_sp_ency_269037() throws JSONException {
        check("1990_sp_ency_269037.xml");
    }
    
    @Test//8, 9
    public void brb_2014_rs_210025() throws JSONException {
        check("brb_2014_rs_210025.xml");
    }
    
    @Test//10
    public void gl17_rs_10071() throws JSONException {
        check("gl17_rs_10071.xml");
    }
    
    private void check(String fileName) throws JSONException {
        check(FOLDER, fileName);
    }
    
    private void check(String folder, String fileName) throws JSONException {
        Article entry = create();
        Document document = readDocument(folder + fileName);
        //Document document = TestUtils.readDocument(folder + "/" + fileName);
        ArticleXmlParser.extractFieldsFromXml(entry, new XmlReader(document), new HashSet<>() {
            @Override
            public boolean contains(Object o) {
                return true;
            }
        });
        
        String actualJson = TestUtils.toJSON(entry);
        
        String jsonFileName = fileName.replace(".xml", ".json");
        
        //TestUtils.writeStringToFile(Paths.get("D:\\workspaces\\0\\platform.shared.cms-import\\cm-importer\\src\\test\\resources\\", folder, jsonFileName), actualJson);
        
        String expectedJson = TestUtils.readResourceFile(folder + jsonFileName);
        
        TestUtils.assertJsonEquals(expectedJson, actualJson);
    }
    
    private static Article create() {
        Article result = Article.builder().build();
        result.setTitle("Mock Article");
        return result;
    }
    
    public static Document readDocument(String relativePath) {
        Document document = null;
        try(InputStream in = TestUtils.class.getResource(relativePath).openStream()) {
            document = Jsoup.parse(in, "UTF-8", "", Parser.xmlParser());
        } catch (Exception parseException) {
            throw new RuntimeException(parseException);
        }
        return document;
    }
}
