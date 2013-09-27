package lithiumtact;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Alex Wu
 */
@XmlRootElement(name = "searchResults")
@XmlAccessorType(XmlAccessType.FIELD)
public class SearchResults {
	List<SearchResult> results = new ArrayList<SearchResult>();

	public SearchResults addResult(SearchResult result) {
		results.add(result);
		return this;
	}
}
