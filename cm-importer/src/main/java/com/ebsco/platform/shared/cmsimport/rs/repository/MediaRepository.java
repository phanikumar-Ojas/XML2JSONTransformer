package com.ebsco.platform.shared.cmsimport.rs.repository;

import com.ebsco.platform.shared.cmsimport.rs.domain.Media;
import com.ebsco.platform.shared.cmsimport.rs.util.HtmlToJsonRteConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

@RequiredArgsConstructor
@Log4j2
public class MediaRepository {

	private static final String QUERY = """
		SELECT tf.image_file, tf.source, tf.terms, tf.copyright, tf.rights_note, tf.caption, tf.image_content_type,
		 tf.webpage_url, tf.position, tf.file_type, tf.not_placard, tf.force_placard
	        FROM t_figures tf
	        WHERE tf.article_id IN (SELECT article_id
	        	FROM t_articles ta, t_books tb
	           WHERE ta.book_id = tb.book_id and ta.do_not_use = 'FALSE' and tb.mid in ('GY4R', 'GY4P', 'HDMJ', 'GYF8', 'GY4S', 'GY4Q'))
	        AND tf.image_file IS NOT NULL
	""";

	private static final String TERMS_QUERY = """
		 SELECT DISTINCT t.term FROM t_lookup_terms t WHERE t.type='PlacardImg' AND t.term IS NOT NULL
	""";

   private final DatabaseClient databaseClient;

   public List<Media> find() {
       try (Statement statement = databaseClient.getConnection().createStatement()) {
    	   log.info(" Reading terms from db ...");
    	   ResultSet termsRs = statement.executeQuery(TERMS_QUERY);
    	   log.info(" ok");
    	   Collection<String> terms = new HashSet<>();
    	   while (termsRs.next()) {
    		   terms.add(termsRs.getString("term"));
    	   }
    	   log.info(" Reading images from db...");
           ResultSet rs = statement.executeQuery(QUERY);
           log.info(" ok");
           List<Media> result = new ArrayList<>();
           log.info("Building entities ...");
           
           Map<String, Media.Asset> filename2Asset = new HashMap<>();
		   while (rs.next()) {
			   Media entity = new Media();
			   
			   String caption = rs.getString("caption");
			   String imageFile = rs.getString("image_file");
               Media.Asset asset = filename2Asset.get(imageFile);
               if (Objects.isNull(asset)) {
                   filename2Asset.put(imageFile, asset = new Media.Asset());
                   asset.setDescription(caption);
                   asset.setFilename(imageFile);
               }
               
               if (StringUtils.isBlank(asset.getDescription()) && StringUtils.isNotBlank(caption)) {
                   asset.setDescription(caption);
               }
               
               entity.setAsset(asset);
			   
			   entity.setTitle(imageFile);
			   entity.setVendorSource(rs.getString("source"));
			   entity.setRights(rs.getString("terms"));
			   entity.setCopyrightNotes(HtmlToJsonRteConverter.convertFromHtml(rs.getString("copyright")));
			   entity.setLicense(HtmlToJsonRteConverter.convertFromHtml(rs.getString("rights_note")));
			   entity.setCaption(HtmlToJsonRteConverter.convertFromHtml(caption));
			   entity.setCredit(HtmlToJsonRteConverter.convertFromHtml(rs.getString("copyright")));
			   entity.setContentType(resolveImageContentType(rs.getString("image_content_type")));
			   entity.setAltText(HtmlToJsonRteConverter.convertFromHtml(rs.getString("caption")));
			   String webpageUrl = rs.getString("webpage_url");
			   if (Objects.nonNull(webpageUrl)) {
				   entity.setWebpageUrl(Set.of(webpageUrl));
			   }
			   entity.setPositionInline(rs.getString("position"));
			   
			   String fileType = rs.getString("file_type");
			   if (Objects.nonNull(fileType)) {
			       entity.setFormat(fileType.toLowerCase());
			   }
			   
			   entity.setPlacardImage(terms.contains(imageFile));
			   
			   String notPlacard = rs.getString("not_placard");
			   if (Objects.nonNull(notPlacard)) {
			       entity.setNotPlacard(BooleanUtils.toBoolean(notPlacard));
			       
			   }
			   
			   String forcePlacard = rs.getString("force_placard");
               if (Objects.nonNull(forcePlacard)) {
                   entity.setForcePlacard(BooleanUtils.toBoolean(forcePlacard));
               }
			   result.add(entity);
		   }
           return result;
       } catch (SQLException e) {
           throw new RuntimeException(e);
       }
   }

	private static String resolveImageContentType(String imageContentType) {
		if (imageContentType == null) {
			return null;
		}
		return switch (imageContentType.toLowerCase()) {
			case "black and white photograph", "black anc white photograph", "black and white photo",
					"black and white photogaph", "black and white photoraph", "blank and white photograph",
					"black and white  photograph", "black and white photograph.", "blalck and white photograph",
					"b/w photo", "b/w photos", "black and white engraving", "lack", "b" -> "B/W Photo";
			case "color photograph", "color reproduction", "color photo reproduction", "colorphotograph",
					"color photographer", "color photograph.", "color photgraph", "color photograph/video",
					"color phtograph", "colodr photograph", "color engraving", "color photogrraph",
					"color woodblock", "color photos", "color", "color photo and artist rendering",
					"color photograph (video)", "color photo", "lor photograph", "olo", "c", "author photo",
					"sepia photograph", "photo", "photograph", "brooklyn museum 22.1403 wrist guard igitembe" ->
					"Color Photo";
			case "illustration", "ilustration", "illustration.", "color illustration", "illustration with text",
					"black and white illustration", "drawing", "color drawing", "color painting", "painting", "art",
					"cartoon", "aart", "cartoon representation", "artt", "poster art", "albumin print", "color image",
					"color print", "rt", "engraving reproduction", "flag" -> "Illustration";
			case "color photo collage", "photo collage", "photo collage with text" -> "Photo Collage";
			case "map", "maps", "color map", "mqp", "m" -> "Map";
			case "graph", "infographic", "animated graph", "animation of diagram", "graphic", "bar chart", "diagram.",
					"diagram", "color diagram", "color graphic", "chart", "gaph", "animated diagram",
					"diagram (animation)", "animated diagram.", "animation", "animated image", "animated .gif", "logo",
					"graphic design", "screenshot", "coding string", "dia", "icon",
					"black and white photograph and charts" -> "Infographic";
			case "table" -> "Table";
			case "illustration/photo collage" -> "Illustration/Photo Collage";
			case "color photo with text", "photo with text", "text", "\"literary work\" (text)" -> "Photo with Text";
			case "equation" -> "Equation";
			case "audio" -> "Audio";
			case "book cover", "book jacket" -> "Book Jacket";
			default -> null;
		};
	}
}
