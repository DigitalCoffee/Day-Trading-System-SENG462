--
-- PostgreSQL database dump
--

-- Dumped from database version 9.6devel
-- Dumped by pg_dump version 9.6devel

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: buy; Type: TABLE; Schema: public; Owner: dbayly
--

CREATE TABLE buy (
    name character varying(8),
    amount integer,
    ownerid character varying(40)
);


ALTER TABLE buy OWNER TO dbayly;

--
-- Name: quote; Type: TABLE; Schema: public; Owner: dbayly
--

CREATE TABLE quote (
    amount integer,
    cryptkey character varying(40),
    "timestamp" bigint,
    name character varying(3),
    ownerid character varying(40)
);


ALTER TABLE quote OWNER TO dbayly;

--
-- Name: sell; Type: TABLE; Schema: public; Owner: dbayly
--

CREATE TABLE sell (
    ownerid character varying(80),
    symbol character varying(3),
    amount double precision
);


ALTER TABLE sell OWNER TO dbayly;

--
-- Name: stock; Type: TABLE; Schema: public; Owner: dbayly
--

CREATE TABLE stock (
    ownerid character varying(80),
    name character varying(3),
    amount integer
);


ALTER TABLE stock OWNER TO dbayly;

--
-- Name: trigger; Type: TABLE; Schema: public; Owner: dbayly
--

CREATE TABLE trigger (
    id character varying(4),
    sname character varying(3),
    price double precision,
    amount integer,
    account double precision,
    bors character varying(10)
);


ALTER TABLE trigger OWNER TO dbayly;

--
-- Name: users; Type: TABLE; Schema: public; Owner: dbayly
--

CREATE TABLE users (
    id character varying(80) NOT NULL,
    account real
);


ALTER TABLE users OWNER TO dbayly;

--
-- Data for Name: buy; Type: TABLE DATA; Schema: public; Owner: dbayly
--

COPY buy (name, amount, ownerid) FROM stdin;
\.


--
-- Data for Name: quote; Type: TABLE DATA; Schema: public; Owner: dbayly
--

COPY quote (amount, cryptkey, "timestamp", name, ownerid) FROM stdin;
\.


--
-- Data for Name: sell; Type: TABLE DATA; Schema: public; Owner: dbayly
--

COPY sell (ownerid, symbol, amount) FROM stdin;
\.


--
-- Data for Name: stock; Type: TABLE DATA; Schema: public; Owner: dbayly
--

COPY stock (ownerid, name, amount) FROM stdin;
\.


--
-- Data for Name: trigger; Type: TABLE DATA; Schema: public; Owner: dbayly
--

COPY trigger (id, sname, price, amount, account, bors) FROM stdin;
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: dbayly
--

COPY users (id, account) FROM stdin;
\.


--
-- Name: users_pkey; Type: CONSTRAINT; Schema: public; Owner: dbayly
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: sell_ownerid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: dbayly
--

ALTER TABLE ONLY sell
    ADD CONSTRAINT sell_ownerid_fkey FOREIGN KEY (ownerid) REFERENCES users(id);


--
-- Name: stock_ownerid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: dbayly
--

ALTER TABLE ONLY stock
    ADD CONSTRAINT stock_ownerid_fkey FOREIGN KEY (ownerid) REFERENCES users(id);


--
-- Name: public; Type: ACL; Schema: -; Owner: dbayly
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM dbayly;
GRANT ALL ON SCHEMA public TO dbayly;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

