DROP TABLE IF EXISTS groups CASCADE;
CREATE TABLE groups (
    id SERIAL NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT groups_pkey PRIMARY KEY (id),
    CONSTRAINT groups_name_ukey UNIQUE (name)
);
INSERT INTO groups(id, name) VALUES (0, 'DEFAULT GROUP');

DROP TABLE IF EXISTS students CASCADE;
CREATE TABLE students (
    id SERIAL NOT NULL,
    group_id INT NOT NULL DEFAULT (0),
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    CONSTRAINT students_pkey PRIMARY KEY (id),
    CONSTRAINT students_group_fkey FOREIGN KEY (group_id) REFERENCES groups (id)
);

DROP TABLE IF EXISTS courses CASCADE;
CREATE TABLE courses (
    id SERIAL NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    CONSTRAINT courses_pkey PRIMARY KEY (id),
    CONSTRAINT courses_name_ukey UNIQUE (name)
);

DROP TABLE IF EXISTS students_courses CASCADE;
CREATE TABLE students_courses (
    student_id INT NOT NULL,
    course_id INT NOT NULL,
    CONSTRAINT students_courses_pkey PRIMARY KEY (student_id, course_id),
    CONSTRAINT students_courses_students_id_fkey FOREIGN KEY (student_id) REFERENCES students (id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT students_courses_course_id_fkey FOREIGN KEY (course_id) REFERENCES courses (id) ON UPDATE CASCADE ON DELETE CASCADE
);