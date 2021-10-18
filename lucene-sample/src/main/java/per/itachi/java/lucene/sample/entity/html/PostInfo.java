package per.itachi.java.lucene.sample.entity.html;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PostInfo {

	private long categoryId;
	
    private String title;

    private String addressLink;

    private String cdate;

    private String edate;
}
