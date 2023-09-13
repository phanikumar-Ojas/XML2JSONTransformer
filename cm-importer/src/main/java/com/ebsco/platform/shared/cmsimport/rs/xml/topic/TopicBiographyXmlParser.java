package com.ebsco.platform.shared.cmsimport.rs.xml.topic;

import static com.ebsco.platform.shared.cmsimport.rs.xml.XPathValueToPojoExtractor.MappingConfig.MULTIPLE_STRING_VALUES_CONVERTER;
import static java.util.Map.entry;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicBiography;
import com.ebsco.platform.shared.cmsimport.rs.xml.JsonSchemaValueConfig;
import com.ebsco.platform.shared.cmsimport.rs.xml.XPathValueToPojoExtractor;
import com.ebsco.platform.shared.cmsimport.rs.xml.XPathValueToPojoExtractor.MappingConfig;
import com.ebsco.platform.shared.cmsimport.rs.xml.XPathValueToPojoExtractor.MappingConfig.FieldConfig;
import com.ebsco.platform.shared.cmsimport.rs.xml.XmlToJsonRteConvertor;

public class TopicBiographyXmlParser implements TopicXmlParser<TopicBiography> {
	
	private static final String CURRICULUM_PATTERN = "//book-part-categories/subj-group[@subj-group-type='curriculum']"
			+ "/subject[contains(text(), '%s')]/parent::subj-group/subj-group/subject";
	
	private static final Map<String, String> BOOKID_2_RACE = Map.ofEntries(
			entry("rrai_rs", "American Indian"),
			entry("glaa_sp_ency_bio", "African American"),
			entry("glja_rs", "Jewish"),
			entry("gll_rs", "Latino")
	);
	
	private static final Map<String, String> BOOKID_2_OTHER_OCCUPATION_RELATED_FIELDS = Map.ofEntries(
			entry("invc_rs", "Technology"),
			entry("cbbioec_sp_ency_bio", "Science"),
			entry("csd_rs",	"Literature"),
			entry("cspe_sp_ency_bio", "Literature, Poetry"),
			entry("cssfw_rs", "Literature"),
			entry("cwa_rs","Literature"),
			entry("ell_2221_rs", "Literature"),
			entry("math_sp_ency_sci", "Science"),
			entry("supc_rs", "Politics"),
			entry("athletes_rs", "Sports"),
			entry("phi_sp_ency_bio", "Philosopher"),
			entry("rsbios_arts", "Art"),
			entry("gli_rs",	"Science"),
			entry("glss_rs", "Science"),
			entry("lm_rs", "Literature"),
			entry("ini_sp_ency_bio", "Literature"),
			entry("invi_rs", "Science and Technology"),
			entry("monkeyshines_e92_rs", "Art"),
			entry("monkeyshines_vux_rs", "Inventor"),
			entry("monkeyshines_e96_rs", "Music"),
			entry("musc_sp_ency_bio", "Music")
	);
	
	private static final MappingConfig FIELD_MAPPING_CFG = new MappingConfig().
		of("category", MULTIPLE_STRING_VALUES_CONVERTER, "//book-part/book-part-meta/book-part-categories/subj-group[@subj-group-type='category']/subject[1]").
		of("secondary_category", MULTIPLE_STRING_VALUES_CONVERTER, "//book-part/book-part-meta/book-part-categories/subj-group[@subj-group-type='category']/subj-group/subject").
		
		
		of(new JsonSchemaValueConfig(TopicBiography.CONTENT_TYPE_UID).of(
				FieldConfig.of("curriculum_american_history", String.format(CURRICULUM_PATTERN, "American History")))
					.aliases(
						Map.of(
								"American Civil War & Reconstruction Era (1856-1877)", "US Civil War & Reconstruction Era (1856-1877)",
								"American History 1816-1855", "US History 1816-1855"
						))).
		
		of("curriculum_ancient_history", MULTIPLE_STRING_VALUES_CONVERTER, String.format(CURRICULUM_PATTERN, "Ancient History")).
		of("curriculum_western_civilization_european_history", MULTIPLE_STRING_VALUES_CONVERTER, String.format(CURRICULUM_PATTERN, "Western Civilization/European History")).
		of("curriculum_women_s_history", MULTIPLE_STRING_VALUES_CONVERTER, String.format(CURRICULUM_PATTERN, "Women")).
		of("curriculum_world_history", MULTIPLE_STRING_VALUES_CONVERTER, String.format(CURRICULUM_PATTERN, "World History")).
		of("geographical_category", MULTIPLE_STRING_VALUES_CONVERTER, "//book-part-categories/subj-group[@subj-group-type='geographical']/subject[1]").
		of("sub_geographical_category", MULTIPLE_STRING_VALUES_CONVERTER, "//book-part-categories/subj-group[@subj-group-type='geographical']/subj-group/subject[2]").
		
		of("geo_keyword", MULTIPLE_STRING_VALUES_CONVERTER, "//book-part/book-part-meta/book-part-categories/subj-group[@subj-group-type='geo-keyword']/subject").
		
		of("biography_type", 
			 elements -> {
		 			Element element = ((Elements) elements).first();
					if (Objects.nonNull(element)) {
						return TopicBiography.MULTIPLE_PEOPLE_TYPE;
					}
					return TopicBiography.SINGLE_PEOPLE_TYPE;
				},
				"//book-front/sec/list[@list-content='bio_meta'][2]").
		
		of(CommonFieldConfigs.ALSO_KNOWN_AS).
		
		of("gender", elements -> "afl_rs".equals(bookId((Elements) elements))  ? Arrays.asList("Female") : null,
				CommonFieldConfigs.BOOK_ID_XPATH).//try this first
		
		of("gender", elements -> { 
		 			Element element = ((Elements) elements).first();
					if (Objects.nonNull(element)) {
						return Arrays.asList(RegExUtils.removeFirst(element.ownText(), "\\[GEN]"));
					}
					return null;
				},
				"//index-term/primary[contains(text(), 'GEN')]").//try this second
		
		
		of("main_occupation_or_related_field", MULTIPLE_STRING_VALUES_CONVERTER, "//book-front/sec[@sec-type='a_head']/p").
		
		of("other_occupation_related_fields", elements -> {
				String value = BOOKID_2_OTHER_OCCUPATION_RELATED_FIELDS.get(bookId((Elements) elements));
				return Objects.nonNull(value) ? Arrays.asList(value) : null;
			},
			CommonFieldConfigs.BOOK_ID_XPATH).
		
		of("race", elements -> {
				String value = BOOKID_2_RACE.get(bookId((Elements) elements));
				return Objects.nonNull(value) ? Arrays.asList(value) : null;
			},
			CommonFieldConfigs.BOOK_ID_XPATH).
		
		of("associated_figures", MULTIPLE_STRING_VALUES_CONVERTER, "//ext-link[@ext-link-type='xrefmolp']").
		
		of("principal_works", new XmlToJsonRteConvertor(), "//sec/title[contains(text(), 'Principal Works')]/ancestor::sec").//RTE ex.: rsspencyclopedia_236553, brb_2014_rs_210025
		
		of("bio_reference_id", MULTIPLE_STRING_VALUES_CONVERTER, CommonFieldConfigs.BIO_REF_XPATH);
	
	@Override
	public Output<TopicBiography> parse(Input input, TopicBiography writeTo) {
		Document readFrom = input.getDocument();
		
		XPathValueToPojoExtractor simpleValuesExtractor = new XPathValueToPojoExtractor();
		simpleValuesExtractor.extract(readFrom, writeTo, FIELD_MAPPING_CFG);
		
		readFrom.selectXpath("//book-front/sec[@sec-type='a_head']/p");
		
		if (TopicBiography.SINGLE_PEOPLE_TYPE.equals(writeTo.getBiographyType())) {
			Element birthElem = readFrom.selectXpath("//book-front/sec/list[@list-content='bio_meta'][1]/list-item/label["
					+ "contains(text(), 'Born') or "
					+ "contains(text(), 'Birth Date') or "
					+ "contains(text(), 'Birth date') or "
					+ "contains(text(), 'Date of Birth') or "
					+ "contains(text(), 'Date of birth') or "
					+ "contains(text(), 'Birth:') ]/ancestor::list-item/p").first();
			
			if (Objects.isNull(birthElem)) {
				birthElem = readFrom.selectXpath("//book-front/sec/p/node()["
						+ "contains(text(), 'Born') or "
						+ "contains(text(), 'Birth Date') or "
						+ "contains(text(), 'Birth date') or "
						+ "contains(text(), 'Date of Birth') or "
						+ "contains(text(), 'Date of birth') or "
						+ "contains(text(), 'Birth:') ]/ancestor::p").first();
			}
			
			
			setBirthDateInfo(birthElem, writeTo);
			
			Element placeOfBirth = readFrom.selectXpath("//book-front/sec/list[@list-content='bio_meta'][1]/list-item/label["
					+ "contains(text(), 'Birthplace') or "
					+ "contains(text(), 'Place of birth')]/ancestor::list-item/p").first();
			setBirthPlaceInfo(placeOfBirth, writeTo);
			
			//varitions of text: Death Date, Date of Death, Death
			Element dethElem = readFrom.selectXpath("//book-front/sec/list[@list-content='bio_meta'][1]/list-item/label["
								+ "contains(text(), 'Death Date') or "
								+ "contains(text(), 'Died:') or "
								+ "contains(text(), 'Date of Death') or "
								+ "contains(text(), 'Death:')]/ancestor::list-item/p").first();
			//Death date:
			if (Objects.isNull(dethElem)) {
				dethElem = readFrom.selectXpath("//book-front/sec/p/node()["
						+ "contains(text(), 'Death Date') or "
						+ "contains(text(), 'Death date') or "
						+ "contains(text(), 'Died:') or "
						+ "contains(text(), 'Date of Death') or "
						+ "contains(text(), 'Death:')]/ancestor::p").first();
			}
			
			setDethDateInfo(dethElem, writeTo);
			
			//Variations of text: Deathplace, Place of death, Died 
			Element placeOfDeth = readFrom.selectXpath("//book-front/sec/list[@list-content='bio_meta'][1]/list-item/label["
					+ "contains(text(), 'Deathplace') or "
					+ "contains(text(), 'Place of death')]/ancestor::list-item/p").first();
			setDethPlaceInfo(placeOfDeth, writeTo);
		}
		
		new BoundedAndOrderedTagsExtractor().extract(readFrom, writeTo);
		
		if (Objects.nonNull(writeTo.getCategory())) {
			if (CollectionUtils.isNotEmpty(writeTo.getSecondaryCategory())) {
				writeTo.getSecondaryCategory().removeAll(writeTo.getCategory());
			}
		}
		
		return Output.<TopicBiography>builder().input(input).writeTo(writeTo).build();
	}
	
	public static void setBirthDateInfo(Element birthElem, TopicBiography writeTo) {
		if (Objects.nonNull(birthElem)) {
			String birthDateText = StringUtils.trim(birthElem.ownText());
			if(StringUtils.startsWith(birthDateText, ":")) {
				birthDateText = RegExUtils.removeFirst(birthDateText, ":");
			}
			birthDateText = StringUtils.trim(birthDateText);
			
			writeTo.setNonNumericBirthDate(birthDateText);
			
			LocalDate birthDate = from(birthDateText);
			 if (Objects.nonNull(birthDate)) {
				 writeTo.setBirth_day(birthDate.getDayOfMonth());
				 writeTo.setBirth_month(birthDate.getMonth().getDisplayName(TextStyle.FULL, Locale.US));
				 writeTo.setBirth_year(birthDate.getYear());
			 }
		}
	}
	
	public static void setBirthPlaceInfo(Element placeOfBirth, TopicBiography writeTo) {
		if (Objects.nonNull(placeOfBirth)) {
			String value = placeOfBirth.ownText();
			writeTo.setPlace_of_birth(value);
		}
	}
	
	public static void setDethDateInfo(Element dethElem, TopicBiography writeTo) {
		if (Objects.nonNull(dethElem)) {
			String dethDateText = StringUtils.trim(dethElem.ownText());
			if(StringUtils.startsWith(dethDateText, ":")) {
				dethDateText = RegExUtils.removeFirst(dethDateText, ":");
			}
			dethDateText = StringUtils.trim(dethDateText);
			
			
			writeTo.setNon_numeric_death_date(dethDateText);
			
			LocalDate dethDate = from(dethDateText);
			if (Objects.nonNull(dethDate)) {
			 writeTo.setDeath_day(dethDate.getDayOfMonth());
			 writeTo.setDeath_month(dethDate.getMonth().getDisplayName(TextStyle.FULL, Locale.US));
			 writeTo.setDeath_year(dethDate.getYear());
			}
		}
	}
	
	public static void setDethPlaceInfo(Element placeOfDeth, TopicBiography writeTo) {
		if (Objects.nonNull(placeOfDeth)) {
			String value = placeOfDeth.ownText();
			writeTo.setPlace_of_death(value);
		}
	}

    private static LocalDate from(String dateString) {
    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
    	if (dateString != null && !dateString.isBlank()) {
            try {
            	return LocalDate.parse(dateString, formatter);
            } catch (Exception e) {
            	return null;
            }
        }
        return null;
    }
    
    private static String bookId(Elements elements) {
    	String value = elements.first().ownText();
    	int lastSlashIdx = value.lastIndexOf('/');
    	if (lastSlashIdx != -1) {
    		value = value.substring(lastSlashIdx + 1);
    	}
    	return value;
    }
}
