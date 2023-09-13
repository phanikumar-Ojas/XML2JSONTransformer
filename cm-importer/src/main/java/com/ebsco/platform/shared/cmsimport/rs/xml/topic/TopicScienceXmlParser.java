package com.ebsco.platform.shared.cmsimport.rs.xml.topic;

import static java.util.Map.entry;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jsoup.nodes.Document;

import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicScience;
import com.ebsco.platform.shared.cmsimport.rs.xml.JsonSchemaValueConfig;
import com.ebsco.platform.shared.cmsimport.rs.xml.XPathValueToPojoExtractor;
import com.ebsco.platform.shared.cmsimport.rs.xml.XPathValueToPojoExtractor.MappingConfig;
import com.ebsco.platform.shared.cmsimport.rs.xml.XPathValueToPojoExtractor.MappingConfig.FieldConfig;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class TopicScienceXmlParser implements TopicXmlParser<TopicScience> {
	
	private static final Map<String, String> AVAILABLE_CATEGORIES = Set.of(
			"Animal Science",
			"Applied Sciences",
			"Astronomy",
			"Astrophysics",
			"Biology, Life Sciences, Ecosystems",
			"Chemistry",
			"Computer Science",
			"Disasters",
			"Earth and Atmospheric Sciences",
			"Economics, Business, Production, Commerce",
			"Environmental Sciences",
			"Ethics, Advocacy, Policy, Protest",
			"Geology, Geosciences",
			"Government, Politics, International Relations",
			"Life Sciences",
			"Logic",
			"Mathematics, Measurement, Mapping, Logic",
			"Meteorology, Atmospheric Sciences, Climatology",
			"Physics",
			"Places, Peoples, Societies",
			"Plant Science",
			"Popular Culture, Media, Publishing",
			"Pseudoscience",
			"Technology and Applied Science"
	).stream().collect(Collectors.toMap(key -> key, key -> key));
	static {
		AVAILABLE_CATEGORIES.putAll(
				Map.of(
						"Biology, Life Sciences, Ecology", "Biology, Life Sciences, Ecosystems",
						"Places, People, Societies", "Places, Peoples, Societies",
						"Places, Peoples, and Societies", "Places, Peoples, Societies",
						"Mathematics, Measurement, Mapping", "Mathematics, Measurement, Mapping, Logic"
				));
	}
	
	private static final Map<String, String> AVAILABLE_SECONDARY_CATEGORIES = Set.of(
			"Agriculture, Farming, Animal Husbandry",
			"Animals, Zoology, Marine Biology",
			"Archaeology",
			"Architecture, Construction",
			"Asteroids, Dwarf Planets",
			"Astronomy",
			"Atmospheric Structure and Dynamics",
			"Authors, Writers",
			"Biology, Life Sciences, Ecosystems",
			"Business, Labor",
			"Chemistry",
			"Cities, Urbanization, Urban Planning",
			"Climate and Climate Change",
			"Comets, Meteors, Interplanetary Bodies",
			"Communications",
			"Computers, Computer Science",
			"Conferences, Meetings, Summits",
			"Continents, Regions, Areas, Islands",
			"Cosmology, Astronomic History",
			"Cryology, Glaciology",
			"Disasters",
			"Ecology, Environment, Environmentalism",
			"Economics, Business, Production, Commerce",
			"Education",
			"Elements, Substances, Chemicals, Compounds",
			"Energy and Energy Resources",
			"Engineering, Materials Science",
			"Ethics, Advocacy, Policy, Protest",
			"Fine Arts and Humanities",
			"Food, Hunger",
			"Forests, Forestry",
			"Galaxies, Nebulas, Interstellar Space",
			"Geography, Cartography",
			"Geology, Geosciences",
			"Health, Medicine, Disease, Illness",
			"History, Historiography",
			"Humanitarianism, Philanthropy",
			"Industries, Commercial Products",
			"International Relations, Agreements, Treaties",
			"Inventions",
			"Journalism, Publishing, Literature",
			"Laws, Acts, Resolutions",
			"Mathematics, Measurement, Mapping, Logic",
			"Media, Film, Television",
			"Meteorology, Atmospheric Sciences, Climatology",
			"Mineral Resources",
			"Moon, Satellites",
			"Nations, States, Governments",
			"Nuclear Power, Nuclear Weapons, Radioactive Waste",
			"Oceans, Seas, Lakes, Bodies of Water",
			"Optics, Light, Electromagnetic Radiation",
			"Organizations, Agencies, Commissions, Centers",
			"Paleontology",
			"Parks, Monuments, Reserves",
			"Philosophy and History of Science",
			"Photography, Imaging",
			"Physics",
			"Planets",
			"Plants, Vegetation, Botany, Horticulture",
			"Politicians, Diplomats",
			"Pollution, Emissions, Toxic Waste, Hazardous Materials",
			"Popular Culture, Media, Publishing",
			"Public Policy, Debates, Controversies",
			"Regulation, Standards and Practices",
			"Renewable Resources",
			"Scientific Equipment, Tools, Techniques, and Methods",
			"Scientists, Engineers",
			"Social and Environmental Justice, Human Rights",
			"Social Sciences",
			"Spacecraft, Satellites, Space Exploration",
			"Statistics, Modeling, Projection",
			"Sun, Stars, Stellar Phenomena",
			"Technology and Applied Science",
			"Topography, Topology",
			"Transportation",
			"Trials, Court Decisions, Mediations, Adjudications",
			"Violence and Warfare",
			"Water Resources",
			"Weather"
	).stream().collect(Collectors.toMap(key -> key, key -> key));
	static {
		AVAILABLE_SECONDARY_CATEGORIES.putAll(Map.ofEntries(
				entry("Engineering, Materials Scienc", "Engineering, Materials Science"),
				entry("Education, Educators", "Education"),
				entry("Mathematics, Measurement, Mapping", "Mathematics, Measurement, Mapping, Logic"),
				entry("Social Science", "Social Sciences"),
				entry("Food, HungerForests, Forestry", "Food, Hunger"),
				entry("moon, Satellites", "Moon, Satellites"),
				entry("Climate Change", "Climate and Climate Change"),
				entry("sun, Stars, Stellar Phenomena", "Sun, Stars, Stellar Phenomena"),
				entry("Biology, Life Sciences, Ecosystems [includes ecological degradation]", "Biology, Life Sciences, Ecosystems"),
				entry("Energy Resources", "Energy and Energy Resources")
		));
		//Advocacy, Policy, Protest
		//Finance, Funding, Investment, Development
	}
	
	private static final MappingConfig FIELD_MAPPING_CFG = new MappingConfig()
			.of(new JsonSchemaValueConfig(TopicScience.CONTENT_TYPE_UID).of(
					FieldConfig.of("category", "//book-part-categories/subj-group[@subj-group-type='subjectscience']/subject"))
						.aliases(
							Map.of(
									"Biology, Life Sciences, Ecology", "Biology, Life Sciences, Ecosystems",
									"Places, People, Societies", "Places, Peoples, Societies",
									"Places, Peoples, and Societies", "Places, Peoples, Societies",
									"Mathematics, Measurement, Mapping", "Mathematics, Measurement, Mapping, Logic"
							)))
			
			.of(new JsonSchemaValueConfig(TopicScience.CONTENT_TYPE_UID).of(
					FieldConfig.of("secondary_category", "//book-part-categories/subj-group[@subj-group-type='subjectscience']/subj-group/subject"))
						.aliases(
								Map.ofEntries(
										entry("Engineering, Materials Scienc", "Engineering, Materials Science"),
										entry("Education, Educators", "Education"),
										entry("Mathematics, Measurement, Mapping", "Mathematics, Measurement, Mapping, Logic"),
										entry("Social Science", "Social Sciences"),
										entry("Food, HungerForests, Forestry", "Food, Hunger"),
										entry("moon, Satellites", "Moon, Satellites"),
										entry("Climate Change", "Climate and Climate Change"),
										entry("sun, Stars, Stellar Phenomena", "Sun, Stars, Stellar Phenomena"),
										entry("Biology, Life Sciences, Ecosystems [includes ecological degradation]", "Biology, Life Sciences, Ecosystems"),
										entry("Energy Resources", "Energy and Energy Resources")
								)));

	@Override
	public Output<TopicScience> parse(Input input, TopicScience writeTo) {
		Document readFrom = input.getDocument();
		
		XPathValueToPojoExtractor simpleValuesExtractor = new XPathValueToPojoExtractor();
		simpleValuesExtractor.extract(readFrom, writeTo, FIELD_MAPPING_CFG);
		
		new BoundedAndOrderedTagsExtractor().extract(readFrom, writeTo);
		
		return Output.<TopicScience>builder().input(input).writeTo(writeTo).build();
	}
}
