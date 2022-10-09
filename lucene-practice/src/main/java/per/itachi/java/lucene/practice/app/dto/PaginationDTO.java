package per.itachi.java.lucene.practice.app.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaginationDTO<T> {

    private int totalCount;

    private int pageSize;

    private int pageNbr;

    private List<T> data;
}
