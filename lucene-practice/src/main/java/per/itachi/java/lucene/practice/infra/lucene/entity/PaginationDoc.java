package per.itachi.java.lucene.practice.infra.lucene.entity;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaginationDoc<T> {

    private int totalCount;

    private int pageSize;

    private int pageNbr;

    private List<T> documents;
}
