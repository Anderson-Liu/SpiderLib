/**
 * Created by anderson on 2016/12/15.
 */
public class Book {
    private String marcNo;
    private String bookTitle, bookAuthor, bookType, bookPublisher, bookIsbn;
    private String storeArea, whereNum;
    private int queryTimes, frequency;
    public Book(String marcNo, String bookTitle, String  bookAuthor,
                String bookType, String bookPublisher, String bookIsbn,
                String storeArea, String whereNum, int queryTimes, int frequency){
        this.marcNo = marcNo;
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
        this.bookType = bookType;
        this.bookPublisher = bookPublisher;
        this.bookIsbn = bookIsbn;
        this.storeArea = storeArea;
        this.whereNum = whereNum;
        this.queryTimes = queryTimes;
        this.frequency = frequency;
    }

    public String getMarcNo() {
        return marcNo;
    }

    public void setMarcNo(String marcNo) {
        this.marcNo = marcNo;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }

    public String getBookType() {
        return bookType;
    }

    public void setBookType(String bookType) {
        this.bookType = bookType;
    }

    public String getBookPublisher() {
        return bookPublisher;
    }

    public void setBookPublisher(String bookPublisher) {
        this.bookPublisher = bookPublisher;
    }

    public String getBookIsbn() {
        return bookIsbn;
    }

    public void setBookIsbn(String bookIsbn) {
        this.bookIsbn = bookIsbn;
    }

    public String getWhereNum() {
        return whereNum;
    }

    public void setWhereNum(String whereNum) {
        this.whereNum = whereNum;
    }

    public String getStoreArea() {
        return storeArea;
    }

    public void setStoreArea(String storeArea) {
        this.storeArea = storeArea;
    }

    public int getQueryTimes() {
        return queryTimes;
    }

    public void setQueryTimes(int queryTimes) {
        this.queryTimes = queryTimes;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    @Override
    public String toString() {
        return bookTitle + "," + bookAuthor + "," + bookType + ",queryTimes:" + queryTimes;
    }
}
