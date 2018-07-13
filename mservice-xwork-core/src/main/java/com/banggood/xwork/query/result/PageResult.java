package com.banggood.xwork.query.result;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PageResult implements Serializable {

    private List<?> listData = new ArrayList<>();
    private Integer totalCount;
    private Integer currentPage;
    private Integer totalPage;
    private Integer prevPage;
    private Integer nextPage;
    private Integer pageSize;

    public PageResult(List listData, Integer totalCount, Integer currentPage, Integer pageSize) {
        this.listData = listData;
        this.totalCount = totalCount;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalPage = this.totalCount % this.pageSize == 0 ?
                this.totalCount / this.pageSize : this.totalCount / this.pageSize + 1;
        this.prevPage = this.currentPage - 1 >= 1 ? this.currentPage - 1 : 1;
        this.nextPage =
                this.currentPage + 1 <= this.totalPage ? this.currentPage + 1 : this.totalPage;
    }

    public int getTotalPage() {
        return totalPage == 0 ? 1 : totalPage;
    }

    public static PageResult getEmply(Integer pageSize) {
        return new PageResult(Collections.EMPTY_LIST, 0, 1, pageSize);
    }


    public List<?> getListData() {
        return listData;
    }

    public void setListData(List<?> listData) {
        this.listData = listData;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }

    public Integer getPrevPage() {
        return prevPage;
    }

    public void setPrevPage(Integer prevPage) {
        this.prevPage = prevPage;
    }

    public Integer getNextPage() {
        return nextPage;
    }

    public void setNextPage(Integer nextPage) {
        this.nextPage = nextPage;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public PageResult() {
    }

    @Override
    public String toString() {
        return "PageResult{" +
                "listData=" + listData +
                ", totalCount=" + totalCount +
                ", currentPage=" + currentPage +
                ", totalPage=" + totalPage +
                ", prevPage=" + prevPage +
                ", nextPage=" + nextPage +
                ", pageSize=" + pageSize +
                '}';
    }
}
