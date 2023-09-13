package com.ebsco.platform.shared.cmsimport.rs.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRTE implements RTENode {

	private List<RTENode> children;
	private String type = "doc";
	private String uid;
	private Map<String, Object> attrs;

    @Override
    public void addChild(RTENode child) {
	    if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
    }

    @Override
    public String type() {
        return type;
    }
    
    @Override
    public void type(String type) {
        this.type = type;
    }

    public List<RTENode> children() {
        return children;
    }

    @Data
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class BlockNode implements RTENode {
		
        private List<RTENode> children;
		private String type;
		private String uid;

		private Map<String, Object> attrs;

		public void addChild(RTENode node) {
			if (children == null) {
				children = new ArrayList<>();
			}
			children.add(node);
		}
		
		@Override
		public void children(List<RTENode> children) {
            this.children = children;
        }

        @Override
	    public String type() {
	        return type;
	    }
		
		@Override
	    public void type(String type) {
	        this.type = type;
	    }
		
		@Override
	    public List<RTENode> children() {
	        return children;
	    }

		public void addAttribute(String key, Object value) {
			if (attrs == null) {
				attrs = new HashMap<>();
			}
			attrs.put(key, value);
		}

		public void addAttributes(Map<String, Object> attrs) {
			if (this.attrs == null) {
				this.attrs = new HashMap<>();
			}
			this.attrs.putAll(attrs);
		}
	}

	@Data
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class TextNode implements RTENode {

		private String text;
		private Boolean bold;
		private Boolean italic;
		private Boolean code;
		private Boolean underline;
		private Boolean superscript;
		private Boolean subscript;
		private Boolean strikethrough;

		public void copyStylesFromHolder(TextStyleHolder holder) {
			subscript = holder.getSubscript();
			superscript = holder.getSuperscript();
			underline = holder.getUnderline();
			bold = holder.getBold();
			code = holder.getCode();
			strikethrough = holder.getStrikethrough();
			italic = holder.getItalic();
		}
    }
}
