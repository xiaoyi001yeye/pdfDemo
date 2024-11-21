package ve.pdf.demo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum BookIndexEnum {
    book1("三年级上册", "3A"),
    book2("三年级下册", "3B"),
    book3("四年级上册", "4A"),
    book4("四年级下册", "4B"),
    book5("五年级上册", "5A"),
    book6("五年级下册", "5B"),
    book7("六年级上册", "6A"),
    book8("六年级下册", "6B");

    @Getter
    private String bookName;

    @Getter
    private String index;

    public static BookIndexEnum getBookIndex(String fileName) {
        for (BookIndexEnum bookIndex : BookIndexEnum.values()) {
            if (fileName.contains(bookIndex.getBookName())) {
                return bookIndex;
            }
        }
        return null;
    }


    public static BookIndexEnum getByIndex(String index) {
        for (BookIndexEnum bookIndex : BookIndexEnum.values()) {
            if (bookIndex.getIndex().contains(index)) {
                return bookIndex;
            }
        }
        return null;
    }
}

