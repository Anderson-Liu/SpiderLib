
# 获取学生频数最高的兴趣
select * from frequency_all_without_bookid where stu_id='13100501' order by frequency desc;
# 过滤为book_type_detail,后期再过滤诸如"中国"等倾向性不明显的值
select * from frequency_all_without_bookid where stu_id='13100501' and label_type='book_type_detail' order by frequency desc;

    # 获取在这个标签下Top 20 学生, limit 20?
    select * from frequency_all_without_bookid where label_value='程序设计' order by frequency desc;

        # 获取Top 20学生在这个标签下看的书的bookId 列表
        select * from frequency_all_with_bookid where stu_id='14104538' and label_value='程序设计';
        select book_id from frequency_all_with_bookid where stu_id='14104538' and label_value='程序设计';

            # 获取这些书籍的具体信息
        # select * from all_books where book_id = ?
        select * from all_books where marc_no in
            (select marc_num from book_marc_id where book_id in
                (select book_id from frequency_all_with_bookid where stu_id='14104538' and label_value='程序设计'));
        # ? order by frequency
        # ? order by query_times
                # 对这些书籍进行分析，查看
                    # 是否在Top20学生中有相当高的重复度
                    # 被浏览次数多不多
                    # 被借阅次数多不多
                    # 获取 top1 的所有书籍


    # 获取在这个标签下被借得最多Top20的书, limit 20?
    select * from frequency_marc_books_detail_type where book_detail_type = "程序设计" order by frequency desc;
    # 获取在这个标签下被借得最多的书
    select * from frequency_marc_books_detail_type where book_detail_type = "程序设计" order by query_times desc;
    # ? 建立一个all_books 的detail_type版本的表