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
    stock character varying(8),
    amount integer,
    ownerid character varying(40)
);


ALTER TABLE buy OWNER TO dbayly;

--
-- Name: quote; Type: TABLE; Schema: public; Owner: dbayly
--

CREATE TABLE quote (
    amount money,
    cryptkey character varying(60),
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
    stock character varying(3),
    amount double precision
);


ALTER TABLE sell OWNER TO dbayly;

--
-- Name: stock; Type: TABLE; Schema: public; Owner: dbayly
--

CREATE TABLE stock (
    ownerid character varying(80),
    symbol character varying(3),
    amount integer
);


ALTER TABLE stock OWNER TO dbayly;

--
-- Name: trigger; Type: TABLE; Schema: public; Owner: dbayly
--

CREATE TABLE trigger (
    id character varying(80),
    sname character varying(3),
    price money,
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
    account money
);


ALTER TABLE users OWNER TO dbayly;

--
-- Data for Name: buy; Type: TABLE DATA; Schema: public; Owner: dbayly
--

COPY buy (stock, amount, ownerid) FROM stdin;
\.


--
-- Data for Name: quote; Type: TABLE DATA; Schema: public; Owner: dbayly
--

COPY quote (amount, cryptkey, "timestamp", name, ownerid) FROM stdin;
\.


--
-- Data for Name: sell; Type: TABLE DATA; Schema: public; Owner: dbayly
--

COPY sell (ownerid, stock, amount) FROM stdin;
\.


--
-- Data for Name: stock; Type: TABLE DATA; Schema: public; Owner: dbayly
--

COPY stock (ownerid, symbol, amount) FROM stdin;
sYJRWa0Mih	FCT	8
CqOb5G0hB4	GAM	377
w69hSQMdyy	AUJ	116
xFrzsBbeiS	THF	510
ojovZ9YLZM	AME	260
LwNQpkd8mr	GAM	577
BqhMWgFe1n	XAG	100
ojovZ9YLZM	FCT	11
LwNQpkd8mr	XRS	1235
xFrzsBbeiS	JEM	60
w69hSQMdyy	UTF	1958
CqOb5G0hB4	PXL	443
w69hSQMdyy	FCT	8
CqOb5G0hB4	VNG	28
kul4JJSoHc	CAK	3
jzi5Fg2GGS	MAO	20
xFrzsBbeiS	XAG	23
w69hSQMdyy	JIK	19
umhaEY4lil	PXL	103
jzi5Fg2GGS	PXL	838
umhaEY4lil	JEM	867
sYJRWa0Mih	VNG	2
LwNQpkd8mr	QLE	474
w69hSQMdyy	GTQ	1
w69hSQMdyy	CAK	86
umhaEY4lil	AUJ	191
w69hSQMdyy	ZQG	67
CqOb5G0hB4	CMO	491
kul4JJSoHc	AUJ	113
CqOb5G0hB4	AME	82
jzi5Fg2GGS	FPH	588
w69hSQMdyy	QLE	197
sYJRWa0Mih	ZQG	92
sYJRWa0Mih	PXL	614
LwNQpkd8mr	VZP	52
ojovZ9YLZM	GPV	172
umhaEY4lil	BUK	323
ojovZ9YLZM	UJD	7
ojovZ9YLZM	HWU	16
w69hSQMdyy	IHA	195
ojovZ9YLZM	CAK	85
w69hSQMdyy	MPD	381
xFrzsBbeiS	HLU	343
LwNQpkd8mr	UTF	586
ojovZ9YLZM	HBB	510
CqOb5G0hB4	AUJ	80
LwNQpkd8mr	EWK	841
xFrzsBbeiS	VNG	27
BqhMWgFe1n	CAK	20
LwNQpkd8mr	FCT	5
kul4JJSoHc	FCT	7
kul4JJSoHc	EZK	6
jzi5Fg2GGS	THF	530
jzi5Fg2GGS	BUK	56
BqhMWgFe1n	HWU	44
xFrzsBbeiS	FPH	1015
LwNQpkd8mr	PXL	228
jzi5Fg2GGS	ZQG	59
jzi5Fg2GGS	GAM	854
xFrzsBbeiS	CAK	5
umhaEY4lil	JIK	1
ojovZ9YLZM	WKQ	37
ojovZ9YLZM	MAO	535
sYJRWa0Mih	WKQ	69
xFrzsBbeiS	MAO	89
CqOb5G0hB4	HBB	508
ojovZ9YLZM	THF	1548
jzi5Fg2GGS	HBB	210
CqOb5G0hB4	MPD	47
jzi5Fg2GGS	CAK	19
ojovZ9YLZM	MGF	151
CqOb5G0hB4	ZQG	56
sYJRWa0Mih	HBB	1300
sYJRWa0Mih	UTF	1485
w69hSQMdyy	EWK	558
ojovZ9YLZM	MPD	279
umhaEY4lil	FCT	1
umhaEY4lil	EZK	5
sYJRWa0Mih	RGP	1
CqOb5G0hB4	UTF	812
w69hSQMdyy	HBB	953
w69hSQMdyy	UJD	8
CqOb5G0hB4	GPV	75
umhaEY4lil	CMO	330
jzi5Fg2GGS	JEM	223
umhaEY4lil	HWU	5
LwNQpkd8mr	IHA	380
jzi5Fg2GGS	GTQ	2
ojovZ9YLZM	FPH	402
kul4JJSoHc	WMG	1
BqhMWgFe1n	MPD	379
w69hSQMdyy	RGP	2
w69hSQMdyy	JGF	142
CqOb5G0hB4	FCT	8
sYJRWa0Mih	IHA	323
LwNQpkd8mr	MAO	588
BqhMWgFe1n	PXL	214
kul4JJSoHc	JHH	4
ojovZ9YLZM	QLE	245
kul4JJSoHc	JIK	25
LwNQpkd8mr	AUJ	186
umhaEY4lil	FPH	1254
CqOb5G0hB4	UJD	9
ojovZ9YLZM	JHH	0
umhaEY4lil	JGF	281
LwNQpkd8mr	CMO	211
xFrzsBbeiS	BKM	0
LwNQpkd8mr	JHH	0
w69hSQMdyy	VZP	61
umhaEY4lil	QLE	890
CqOb5G0hB4	IHA	414
umhaEY4lil	GAM	885
w69hSQMdyy	HLU	50
CqOb5G0hB4	HLU	36
BqhMWgFe1n	VZP	46
BqhMWgFe1n	UTF	1333
xFrzsBbeiS	AUJ	0
LwNQpkd8mr	MGF	141
w69hSQMdyy	FPH	333
jzi5Fg2GGS	AME	118
sYJRWa0Mih	BUK	166
umhaEY4lil	UTF	1603
sYJRWa0Mih	EWK	824
xFrzsBbeiS	QLE	522
ojovZ9YLZM	ZQG	2
w69hSQMdyy	WKQ	32
ojovZ9YLZM	IHA	975
jzi5Fg2GGS	EWK	997
CqOb5G0hB4	RGP	3
kul4JJSoHc	THF	881
jzi5Fg2GGS	HLU	171
ojovZ9YLZM	WPJ	502
umhaEY4lil	RGP	2
umhaEY4lil	YGD	0
kul4JJSoHc	HWU	44
jzi5Fg2GGS	UTF	505
kul4JJSoHc	BUK	164
jzi5Fg2GGS	GPV	212
xFrzsBbeiS	JGF	539
umhaEY4lil	NZO	315
ojovZ9YLZM	HLU	67
jzi5Fg2GGS	YGD	6
BqhMWgFe1n	GAM	21
jzi5Fg2GGS	VZP	33
BqhMWgFe1n	VNG	70
xFrzsBbeiS	BUK	51
kul4JJSoHc	YGD	1
xFrzsBbeiS	WPJ	104
BqhMWgFe1n	RGP	0
LwNQpkd8mr	HWU	12
kul4JJSoHc	VNG	82
kul4JJSoHc	CMO	132
BqhMWgFe1n	JHH	0
CqOb5G0hB4	CAK	15
ojovZ9YLZM	BKM	1
sYJRWa0Mih	JGF	448
xFrzsBbeiS	UTF	494
CqOb5G0hB4	THF	220
ojovZ9YLZM	GAM	1036
LwNQpkd8mr	JEM	1808
kul4JJSoHc	NZO	1076
CqOb5G0hB4	JHH	0
umhaEY4lil	WKQ	118
LwNQpkd8mr	MPD	183
sYJRWa0Mih	WPJ	491
umhaEY4lil	XRS	459
BqhMWgFe1n	EZK	9
CqOb5G0hB4	JGF	50
BqhMWgFe1n	HBB	2359
xFrzsBbeiS	UJD	9
kul4JJSoHc	PXL	194
CqOb5G0hB4	BUK	209
LwNQpkd8mr	RGP	2
sYJRWa0Mih	JEM	484
BqhMWgFe1n	WPJ	79
BqhMWgFe1n	JGF	226
w69hSQMdyy	NZO	227
CqOb5G0hB4	WPJ	839
sYJRWa0Mih	GAM	686
kul4JJSoHc	BKM	1
kul4JJSoHc	JEM	70
ojovZ9YLZM	EWK	125
w69hSQMdyy	HWU	46
sYJRWa0Mih	BKM	1
LwNQpkd8mr	WMG	1
xFrzsBbeiS	VZP	11
umhaEY4lil	HLU	87
CqOb5G0hB4	WMG	2
w69hSQMdyy	MAO	225
ojovZ9YLZM	RGP	1
LwNQpkd8mr	JIK	1
LwNQpkd8mr	WPJ	174
xFrzsBbeiS	CMO	213
sYJRWa0Mih	AUJ	95
jzi5Fg2GGS	IHA	201
jzi5Fg2GGS	JGF	40
ojovZ9YLZM	XAG	31
LwNQpkd8mr	VNG	157
sYJRWa0Mih	HWU	20
xFrzsBbeiS	GTQ	1
sYJRWa0Mih	GPV	125
jzi5Fg2GGS	JIK	28
jzi5Fg2GGS	EZK	5
xFrzsBbeiS	EZK	11
jzi5Fg2GGS	AUJ	136
jzi5Fg2GGS	XAG	15
CqOb5G0hB4	VZP	20
umhaEY4lil	THF	356
CqOb5G0hB4	XAG	45
BqhMWgFe1n	AUJ	55
sYJRWa0Mih	HLU	6
ojovZ9YLZM	EZK	51
BqhMWgFe1n	CMO	219
CqOb5G0hB4	XRS	1146
sYJRWa0Mih	XAG	13
xFrzsBbeiS	YGD	1
LwNQpkd8mr	GPV	132
umhaEY4lil	MGF	6
jzi5Fg2GGS	VNG	83
sYJRWa0Mih	XRS	25
w69hSQMdyy	VNG	8
sYJRWa0Mih	MPD	358
kul4JJSoHc	GPV	113
sYJRWa0Mih	QLE	741
sYJRWa0Mih	FPH	1002
kul4JJSoHc	HBB	703
jzi5Fg2GGS	XRS	391
kul4JJSoHc	XAG	20
BqhMWgFe1n	MGF	1119
w69hSQMdyy	AME	316
LwNQpkd8mr	BUK	136
CqOb5G0hB4	EWK	165
LwNQpkd8mr	YGD	1
kul4JJSoHc	IHA	113
w69hSQMdyy	XRS	164
BqhMWgFe1n	QLE	302
xFrzsBbeiS	MGF	592
xFrzsBbeiS	IHA	270
sYJRWa0Mih	NZO	1105
sYJRWa0Mih	THF	589
BqhMWgFe1n	JIK	15
BqhMWgFe1n	NZO	1687
ojovZ9YLZM	VZP	76
sYJRWa0Mih	MGF	1423
CqOb5G0hB4	QLE	653
kul4JJSoHc	MPD	69
CqOb5G0hB4	MGF	672
umhaEY4lil	MPD	121
ojovZ9YLZM	JEM	895
jzi5Fg2GGS	WKQ	11
BqhMWgFe1n	UJD	6
umhaEY4lil	HBB	447
xFrzsBbeiS	GAM	303
LwNQpkd8mr	FPH	237
jzi5Fg2GGS	RGP	1
CqOb5G0hB4	FPH	121
sYJRWa0Mih	JHH	3
ojovZ9YLZM	BUK	62
BqhMWgFe1n	EWK	1110
kul4JJSoHc	GAM	580
LwNQpkd8mr	ZQG	1
CqOb5G0hB4	NZO	2051
ojovZ9YLZM	AUJ	14
xFrzsBbeiS	EWK	924
LwNQpkd8mr	AME	209
CqOb5G0hB4	WKQ	21
LwNQpkd8mr	UJD	16
xFrzsBbeiS	XRS	752
xFrzsBbeiS	WKQ	19
kul4JJSoHc	XRS	378
umhaEY4lil	WPJ	338
CqOb5G0hB4	HWU	23
jzi5Fg2GGS	HWU	4
xFrzsBbeiS	FCT	3
CqOb5G0hB4	MAO	109
kul4JJSoHc	UTF	965
LwNQpkd8mr	HBB	911
ojovZ9YLZM	NZO	781
sYJRWa0Mih	CAK	89
CqOb5G0hB4	JIK	7
w69hSQMdyy	GAM	681
w69hSQMdyy	THF	474
xFrzsBbeiS	NZO	1845
xFrzsBbeiS	AME	12
umhaEY4lil	VNG	26
w69hSQMdyy	JHH	2
sYJRWa0Mih	EZK	0
ojovZ9YLZM	UTF	374
umhaEY4lil	GPV	39
jzi5Fg2GGS	NZO	114
ojovZ9YLZM	XRS	645
BqhMWgFe1n	AME	17
kul4JJSoHc	WKQ	33
BqhMWgFe1n	XRS	267
CqOb5G0hB4	JEM	61
jzi5Fg2GGS	WPJ	144
umhaEY4lil	CAK	45
umhaEY4lil	UJD	5
w69hSQMdyy	PXL	208
umhaEY4lil	VZP	61
ojovZ9YLZM	CMO	181
BqhMWgFe1n	BUK	212
LwNQpkd8mr	NZO	1808
kul4JJSoHc	MAO	115
kul4JJSoHc	ZQG	29
kul4JJSoHc	WPJ	751
jzi5Fg2GGS	UJD	11
w69hSQMdyy	MGF	1397
jzi5Fg2GGS	MPD	262
umhaEY4lil	JHH	-2
jzi5Fg2GGS	QLE	4
LwNQpkd8mr	WKQ	29
umhaEY4lil	IHA	179
kul4JJSoHc	JGF	335
BqhMWgFe1n	HLU	108
xFrzsBbeiS	PXL	273
LwNQpkd8mr	THF	128
BqhMWgFe1n	ZQG	25
sYJRWa0Mih	JIK	27
kul4JJSoHc	VZP	17
umhaEY4lil	AME	268
w69hSQMdyy	WPJ	321
xFrzsBbeiS	HBB	205
ojovZ9YLZM	PXL	24
kul4JJSoHc	HLU	189
w69hSQMdyy	EZK	19
sYJRWa0Mih	AME	263
w69hSQMdyy	JEM	2258
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
kul4JJSoHc	$5,970,189.17
xFrzsBbeiS	$4,262,712.98
w69hSQMdyy	$4,775,357.96
LwNQpkd8mr	$4,990,028.97
BqhMWgFe1n	$5,990,169.41
ojovZ9YLZM	$5,676,706.33
CqOb5G0hB4	$4,816,196.81
sYJRWa0Mih	$5,455,422.64
jzi5Fg2GGS	$3,877,294.06
umhaEY4lil	$5,248,358.09
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

