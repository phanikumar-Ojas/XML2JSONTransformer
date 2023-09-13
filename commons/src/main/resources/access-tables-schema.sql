CREATE TABLE IF NOT EXISTS "t_articles"(
"date_added" TEXT, "rs_pending" TEXT, "article_title" TEXT, "article_id" TEXT,
 "book_id" TEXT, "derived_from_id" TEXT, "article_an" TEXT, "parent_article_id" TEXT,
 "b_an" TEXT, "mfs_an" TEXT, "alt_mid" TEXT, "research_starter" TEXT,
 "primary_article" TEXT, "previous_date" TEXT, "file_name" TEXT, "dtformat" TEXT,
 "word_count" TEXT, "sidebar" TEXT, "image" TEXT, "date_orig" TEXT,
 "update_date" TEXT, "review_date" TEXT, "data_type" TEXT, "art_type" TEXT,
 "file_name_alt" TEXT, "date_in_repository" TEXT, "topic" TEXT, "rs_title" TEXT,
 "?" TEXT, "search_title" TEXT, "xml_version_ui" TEXT, "bio" TEXT,
 "lit" TEXT, "pdf_date_on_bigfoot" TEXT, "update_cycle" TEXT, "status" TEXT,
 "source_note" TEXT, "copied_to_build" TEXT, "updated_build" TEXT, "do_not_use" TEXT,
 "usage_note" TEXT, "primary_preferred" TEXT, "secondary_preferred" TEXT, "consumer_preferred" TEXT,
 "corporate_preferred" TEXT, "academic_preferred" TEXT, "primary_mkt" TEXT, "secondary_mkt" TEXT,
 "consumer_mkt" TEXT, "corporate_mkt" TEXT, "academic_mkt" TEXT, "rs_an" TEXT,
 "rights" TEXT, "product" TEXT, "article_mid" TEXT, "last_update_type" TEXT,
 "assign_project" TEXT, "id_of_primary" TEXT, "date_repository_updated" TEXT, "pdf" TEXT,
 "pdf_date_in_repository" TEXT, "root_an" TEXT, "brst_topic" TEXT, "brst_category" TEXT);

CREATE TABLE IF NOT EXISTS "t_books"(
"book_title" TEXT, "mid" TEXT, "series_id" TEXT, "article_count" TEXT,
 "book_id" TEXT, "series_id_xml" TEXT, "parent_book_id" TEXT, "pub_date" TEXT,
 "source" TEXT, "doctype" TEXT, "quality_rank" TEXT, "reading_level" TEXT,
 "old_edition" TEXT, "base_folder" TEXT, "book_note" TEXT, "pid" TEXT,
 "publisher" TEXT, "edition" TEXT, "rs_title" TEXT, "rs_mid" TEXT,
 "rs_book_id" TEXT, "pub_year" TEXT, "publisher-loc" TEXT, "copyright-statement" TEXT,
 "copyright-holder" TEXT, "isbn" TEXT, "language" TEXT, "date_added" TEXT,
 "content_source" TEXT, "bio" TEXT, "lit" TEXT, "data_format" TEXT,
 "spxml_version_available" TEXT, "rights" TEXT, "old_book_title" TEXT, "book_detail" TEXT);

CREATE TABLE IF NOT EXISTS "t_figures"(
"article_id" TEXT, "image_file" TEXT, "figure_key" TEXT, "image_name" TEXT,
 "caption" TEXT, "description" TEXT, "copyright" TEXT, "source" TEXT,
 "fig_type" TEXT, "image_content_type" TEXT, "ext_link_type" TEXT, "use_image" TEXT,
 "file_path" TEXT, "photographer_illustrator" TEXT, "file_type" TEXT, "orig_id" TEXT,
 "imh_an" TEXT, "order" TEXT, "webpage_url" TEXT, "webfile_url" TEXT,
 "position" TEXT, "rights_note" TEXT, "image_removed_date" TEXT, "print_run_limit" TEXT,
 "print_use_end_year" TEXT, "use_end_date" TEXT, "print_rights_only" TEXT, "orig_or_new" TEXT,
 "location" TEXT, "image_type" TEXT, "terms" TEXT, "replaces" TEXT,
 "image_title" TEXT, "status" TEXT, "image_missing" TEXT, "fig_ui" TEXT,
 "not_placard" TEXT, "not_placard_aws_rules" TEXT, "not_placard_eds_rules" TEXT, "force_placard" TEXT,
 "height" TEXT, "width" TEXT, "size_kb" TEXT, "vendor_image_id" TEXT,
 "use_note" TEXT, "repository_date" TEXT, "bigfoot_date" TEXT, "date_added" TEXT,
 "date_updated" TEXT);

CREATE TABLE IF NOT EXISTS "t_lexiles"(
"article_id" TEXT, "mfs_an" TEXT, "lexile" TEXT, "article_connect_level" TEXT);

CREATE TABLE IF NOT EXISTS "t_product_assignment"(
"an" TEXT, "product" TEXT, "level" TEXT, "date_entered" TEXT,
 "remove_from_product" TEXT, "in_admin" TEXT, "date_removed" TEXT);
 
CREATE TABLE IF NOT EXISTS "t_project_items"(
"article_title" TEXT, "new_title" TEXT, "article_id" TEXT, "project_id" TEXT,
 "task" TEXT, "hold_status" TEXT, "notes" TEXT, "date_entered" TEXT,
 "file_copied_date" TEXT, "chosen_copied_date" TEXT, "item_used" TEXT, "file_name" TEXT,
 "file_name_alt" TEXT, "xml_ett" TEXT, "article_type" TEXT, "item_number" TEXT,
 "main_article" TEXT, "new_article_id" TEXT, "new_an" TEXT, "topic_covered" TEXT,
 "word_ct_orig" TEXT, "word_ct" TEXT, "orig_der" TEXT, "output_format" TEXT,
 "image_needed" TEXT, "image_1" TEXT, "omit_indexing" TEXT, "related_topic_1" TEXT,
 "related_topic_2" TEXT, "toc" TEXT, "new_file_name" TEXT, "copied_to_build" TEXT,
 "updated_build" TEXT, "bio" TEXT, "lit" TEXT, "replace_an" TEXT,
 "child_article" TEXT, "assign_update_project" TEXT, "orig_book_id" TEXT, "sort_key" TEXT,
 "date_title_changed" TEXT, "article_mid" TEXT, "new_issue_date" TEXT, "book_id" TEXT,
 "guidelines" TEXT, "adhoc_update_date" TEXT);
 
CREATE TABLE IF NOT EXISTS "t_manifest"(
"AN" TEXT, "UI" TEXT, "source" TEXT);

CREATE TABLE IF NOT EXISTS "t_lookup_terms"(
"an" TEXT, "type" TEXT, "term" TEXT);

CREATE TABLE IF NOT EXISTS "t_salem_names"(
"KEY" TEXT, "AN" TEXT, "Fullname" TEXT, "First" TEXT,
 "Last" TEXT, "Suffix" TEXT, "Parenthetical" TEXT, "birth_year" TEXT,
 "death_year" TEXT, "LNF_Salem" TEXT, "BIOAN_h1" TEXT, "BIOID_h2" TEXT,
 "SP AN" TEXT, "BIOID_h1" TEXT, "has sp bio" TEXT, "name_fnf" TEXT,
 "name_lnf" TEXT, "birth_date" TEXT, "death_date" TEXT, "bdate_num" TEXT,
 "ddate_num" TEXT, "birth_place" TEXT, "death_place" TEXT, "occ" TEXT,
 "deid" TEXT, "BIOAN_h2" TEXT, "bio1" TEXT, "bio2" TEXT,
 "bio3" TEXT, "bio4" TEXT, "bio5" TEXT, "bio_alt" TEXT,
 "bio_alt2" TEXT, "bio_alt3" TEXT, "force_alt" TEXT, "merge_to" TEXT,
 "merged_date" TEXT, "name_table_id" TEXT, "aka" TEXT, "pronunciation" TEXT,
 "date_entered" TEXT, "update_date" TEXT);
 
 CREATE TABLE IF NOT EXISTS "t_bio_ref"("bio_ref" TEXT, "movement_id" TEXT);
 CREATE TABLE IF NOT EXISTS "t_movement"("id" TEXT, "name" TEXT);
 