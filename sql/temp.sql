CREATE TABLE `ahaulib`.`books` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `book_id` CHAR(30) NOT NULL,
  `book_title` VARCHAR(20) NULL,
  `book_author` VARCHAR(10) NULL,
  `borrow_date` DATE NULL,
  `return_date` DATE NULL,
  `store_area` CHAR(12) NULL,
  PRIMARY KEY (`id`, `book_id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  UNIQUE INDEX `book_id_UNIQUE` (`book_id` ASC));
