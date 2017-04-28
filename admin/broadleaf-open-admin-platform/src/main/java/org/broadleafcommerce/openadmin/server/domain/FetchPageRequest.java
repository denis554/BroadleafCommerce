package org.broadleafcommerce.openadmin.server.domain;

/**
 * Encapsulate params related to fetch paging
 *
 * @author Jeff Fischer
 */
public class FetchPageRequest {

    protected Integer startIndex;
    protected Integer maxIndex;
    protected Long lastId;
    protected Long firstId;
    protected Integer upperCount;
    protected Integer lowerCount;
    protected Integer pageSize;

    public FetchPageRequest withStartIndex(Integer startIndex) {
        setStartIndex(startIndex);
        return this;
    }

    public FetchPageRequest withMaxIndex(Integer maxIndex) {
        setMaxIndex(maxIndex);
        return this;
    }

    public FetchPageRequest withLastId(Long lastId) {
        setLastId(lastId);
        return this;
    }

    public FetchPageRequest withFirstId(Long firstId) {
        setFirstId(firstId);
        return this;
    }

    public FetchPageRequest withUpperCount(Integer upperCount) {
        setUpperCount(upperCount);
        return this;
    }

    public FetchPageRequest withLowerCount(Integer lowerCount) {
        setLowerCount(lowerCount);
        return this;
    }

    public FetchPageRequest withPageSize(Integer pageSize) {
        setPageSize(pageSize);
        return this;
    }

    public Integer getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(Integer startIndex) {
        this.startIndex = startIndex;
    }

    public Integer getMaxIndex() {
        return maxIndex;
    }

    public void setMaxIndex(Integer maxIndex) {
        this.maxIndex = maxIndex;
    }

    public Long getLastId() {
        return lastId;
    }

    public void setLastId(Long lastId) {
        this.lastId = lastId;
    }

    public Long getFirstId() {
        return firstId;
    }

    public void setFirstId(Long firstId) {
        this.firstId = firstId;
    }

    public Integer getUpperCount() {
        return upperCount;
    }

    public void setUpperCount(Integer upperCount) {
        this.upperCount = upperCount;
    }

    public Integer getLowerCount() {
        return lowerCount;
    }

    public void setLowerCount(Integer lowerCount) {
        this.lowerCount = lowerCount;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
