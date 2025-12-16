USE LibraryDB;

--START
START TRANSACTION;

--GENRE
INSERT INTO T_Genre (genre) VALUES
('Fantasy'), ('Action'),
('Database'), ('Fiction'), ('Literary Fiction'), ('Dystopian'), ('Historical Fiction'), ('Contemporary');

--AUTHORS
INSERT INTO T_Author (name, DOB) VALUES
('Andrzej Sapkowski', NULL), ('George R. R. Martin', NULL), ('J. K. Rowling', NULL), ('C. S. Lewis', NULL),
('Catherine Ricardo', '1999-11-11'), ('Susan Ullman', '1999-11-11'), ('Kazuo Ishiguro', '1999-11-11'),
('Margaret Atwood', '1999-11-11'), ('Douglas Stuart', '1999-11-11'), ('Douglas Coupland', '1999-11-11');

--BOOKS
INSERT INTO T_Book (isbn, title, published, storyLine, rating) VALUES
('1000800091730', 'The Witcher', '1999-09-01', 'En monsterjägare letar efter sitt öde.', 4),
('9780553103540', 'A Game of Thrones', '1996-08-06', 'Vinter kommer och alla bråkar om en stol.', 5),
('9780747532743', 'Lord of the rings', '1997-06-26', 'En ring att styra dem alla.', 5),
('9780064471190', 'The Lion, the Witch and the Wardrobe', '1950-10-16', 'Fyra barn hittar ett land i en garderob.', 3),
('1234567890123', 'Databases Illuminated', '2018-01-01', 'A comprehensive guide to database systems.', 5),
('2345678901234', 'Dark Databases', '1990-01-01', 'A thriller about database security.', 1),
('4567890123456', 'The buried giant', '2015-03-01', 'A fantasy novel set in post-Arthurian Britain.', NULL),
('5678901234567', 'Never let me go', '2005-01-01', 'A dystopian science fiction novel.', NULL),
('6789012345678', 'The remains of the day', '1989-05-01', 'A story of an English butler in post-war England.', NULL),
('2345678900000', 'Alias Grace', '1996-09-01', 'Based on true events about a convicted murderess.', 2),
('3456789111111', 'The handmaids tale', '1985-01-01', 'A dystopian novel about a totalitarian society.', 3),
('3456789012222', 'Shuggie Bain', '2020-01-01', 'A story of growing up in 1980s Glasgow.', 4),
('3456789123333', 'Microserfs', '1995-01-01', 'Life at Microsoft in the early 1990s.', 5);

--BOOK AND AUTHOR
INSERT INTO T_BookAuthor (bookId, auId) SELECT b.bookId, a.auId FROM T_Book b JOIN T_Author a WHERE b.title='The Witcher' AND a.name='Andrzej Sapkowski';
INSERT INTO T_BookAuthor (bookId, auId) SELECT b.bookId, a.auId FROM T_Book b JOIN T_Author a WHERE b.title='A Game of Thrones' AND a.name='George R. R. Martin';
INSERT INTO T_BookAuthor (bookId, auId) SELECT b.bookId, a.auId FROM T_Book b JOIN T_Author a WHERE b.title='Lord of the rings' AND a.name='J. K. Rowling';
INSERT INTO T_BookAuthor (bookId, auId) SELECT b.bookId, a.auId FROM T_Book b JOIN T_Author a WHERE b.title='The Lion, the Witch and the Wardrobe' AND a.name='C. S. Lewis';
INSERT INTO T_BookAuthor (bookId, auId) SELECT b.bookId, a.auId FROM T_Book b JOIN T_Author a WHERE b.isbn='1234567890123' AND a.name IN ('Catherine Ricardo', 'Susan Ullman');
INSERT INTO T_BookAuthor (bookId, auId) SELECT b.bookId, a.auId FROM T_Book b JOIN T_Author a WHERE b.isbn='2345678901234' AND a.name='Catherine Ricardo';
INSERT INTO T_BookAuthor (bookId, auId) SELECT b.bookId, a.auId FROM T_Book b JOIN T_Author a WHERE b.isbn IN ('4567890123456', '5678901234567', '6789012345678') AND a.name='Kazuo Ishiguro';
INSERT INTO T_BookAuthor (bookId, auId) SELECT b.bookId, a.auId FROM T_Book b JOIN T_Author a WHERE b.isbn IN ('2345678900000', '3456789111111') AND a.name='Margaret Atwood';
INSERT INTO T_BookAuthor (bookId, auId) SELECT b.bookId, a.auId FROM T_Book b JOIN T_Author a WHERE b.isbn='3456789012222' AND a.name='Douglas Stuart';
INSERT INTO T_BookAuthor (bookId, auId) SELECT b.bookId, a.auId FROM T_Book b JOIN T_Author a WHERE b.isbn='3456789123333' AND a.name='Douglas Coupland';

--BOOK AND GENRE
INSERT INTO T_BookGenre (bookId, genreId) SELECT b.bookId, g.genreId FROM T_Book b JOIN T_Genre g WHERE b.title='The Witcher' AND g.genre='Fantasy';
INSERT INTO T_BookGenre (bookId, genreId) SELECT b.bookId, g.genreId FROM T_Book b JOIN T_Genre g WHERE b.title='A Game of Thrones' AND g.genre='Fantasy';
INSERT INTO T_BookGenre (bookId, genreId) SELECT b.bookId, g.genreId FROM T_Book b JOIN T_Genre g WHERE b.title='Lord of the rings' AND g.genre='Fantasy';
INSERT INTO T_BookGenre (bookId, genreId) SELECT b.bookId, g.genreId FROM T_Book b JOIN T_Genre g WHERE b.title='The Lion, the Witch and the Wardrobe' AND g.genre IN ('Fantasy', 'Action');
INSERT INTO T_BookGenre (bookId, genreId) SELECT b.bookId, g.genreId FROM T_Book b JOIN T_Genre g WHERE b.isbn IN ('1234567890123', '2345678901234') AND g.genre='Database';
INSERT INTO T_BookGenre (bookId, genreId) SELECT b.bookId, g.genreId FROM T_Book b JOIN T_Genre g WHERE b.isbn IN ('2345678901234', '4567890123456', '3456789111111', '3456789123333') AND g.genre='Fiction';
INSERT INTO T_BookGenre (bookId, genreId) SELECT b.bookId, g.genreId FROM T_Book b JOIN T_Genre g WHERE b.isbn IN ('4567890123456', '5678901234567', '6789012345678', '2345678900000', '3456789012222') AND g.genre='Literary Fiction';
INSERT INTO T_BookGenre (bookId, genreId) SELECT b.bookId, g.genreId FROM T_Book b JOIN T_Genre g WHERE b.isbn IN ('5678901234567', '3456789111111') AND g.genre='Dystopian';
INSERT INTO T_BookGenre (bookId, genreId) SELECT b.bookId, g.genreId FROM T_Book b JOIN T_Genre g WHERE b.isbn IN ('6789012345678', '2345678900000') AND g.genre='Historical Fiction';
INSERT INTO T_BookGenre (bookId, genreId) SELECT b.bookId, g.genreId FROM T_Book b JOIN T_Genre g WHERE b.isbn IN ('3456789012222', '3456789123333') AND g.genre='Contemporary';

--SAVE
COMMIT;