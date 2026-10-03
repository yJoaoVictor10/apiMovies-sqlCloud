-- ============================================================
-- API MOVIES
-- DDL - Azure SQL Database
-- ============================================================

IF NOT EXISTS (
    SELECT * FROM sysobjects
    WHERE name = 'category' AND xtype = 'U'
)
BEGIN
CREATE TABLE category (
                          id BIGINT IDENTITY(1,1) PRIMARY KEY,
                          name VARCHAR(255)
);
END;
GO


IF NOT EXISTS (
    SELECT * FROM sysobjects
    WHERE name = 'movie' AND xtype = 'U'
)
BEGIN
CREATE TABLE movie (
                       id BIGINT IDENTITY(1,1) PRIMARY KEY,
                       title VARCHAR(255),
                       synopsis VARCHAR(255),
                       rating INT,
                       release_date DATE,
                       category_id BIGINT,

                       CONSTRAINT FK_movie_category
                           FOREIGN KEY (category_id)
                               REFERENCES category(id)
);
END;
GO