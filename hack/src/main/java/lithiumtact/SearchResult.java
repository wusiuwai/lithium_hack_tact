package lithiumtact;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Alex Wu
 */
@XmlRootElement(name = "searchResult")
@XmlAccessorType(XmlAccessType.FIELD)
public class SearchResult {
	String url;
	String desc;
	String author;
	String created;
	String fileType;
	String title;
	String jiraType;
	String jiraGroup;
	String confluenceSpace;
}
