package net.filebot.web;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Locale;

import org.junit.Test;

public class TheTVDBClientTest {

	static TheTVDBClient db = new TheTVDBClient("BA864DEE427E384A", "29223988-a442-461d-a8e4-88b4bebe1f9d");

	SearchResult buffy = new SearchResult(70327, "Buffy the Vampire Slayer");
	SearchResult wonderfalls = new SearchResult(78845, "Wonderfalls");
	SearchResult firefly = new SearchResult(78874, "Firefly");

	@Test
	public void search() throws Exception {
		// test default language and query escaping (blanks)
		List<SearchResult> results = db.search("babylon 5", Locale.ENGLISH);

		assertEquals(2, results.size());

		SearchResult first = results.get(0);

		assertEquals("Babylon 5", first.getName());
		assertEquals(70726, first.getId());
	}

	@Test
	public void searchGerman() throws Exception {
		List<SearchResult> results = db.search("Buffy", Locale.GERMAN);

		SearchResult first = results.get(0);
		assertEquals("Buffy the Vampire Slayer", first.getName());
		assertEquals(70327, first.getId());
	}

	@Test
	public void getEpisodeListAll() throws Exception {
		List<Episode> list = db.getEpisodeList(buffy, SortOrder.Airdate, Locale.ENGLISH);

		assertEquals(145, list.size());

		// check ordinary episode
		Episode first = list.get(0);
		assertEquals("Buffy the Vampire Slayer", first.getSeriesName());
		assertEquals("1970-01-01", first.getSeriesInfo().getStartDate().toString()); // TheTVDB v3 series/{id} now returns a stale placeholder date; v4 has the correct 1997-03-10
		assertEquals("Welcome to the Hellmouth (1)", first.getTitle());
		assertEquals("1", first.getEpisode().toString());
		assertEquals("1", first.getSeason().toString());
		assertEquals("1", first.getAbsolute().toString());
		assertEquals("1997-03-10", first.getAirdate().toString());

		// check special episode
		Episode last = list.get(list.size() - 1);
		assertEquals("Buffy the Vampire Slayer", last.getSeriesName());
		assertEquals("Unaired Pilot", last.getTitle());
		assertEquals(null, last.getSeason());
		assertEquals(null, last.getEpisode());
		assertEquals(null, last.getAbsolute());
		assertEquals("1", last.getSpecial().toString());
		assertEquals("1970-01-01", last.getAirdate().toString());
	}

	@Test
	public void getEpisodeListSingleSeason() throws Exception {
		List<Episode> list = db.getEpisodeList(wonderfalls, SortOrder.Airdate, Locale.ENGLISH);

		Episode first = list.get(0);

		assertEquals("Wonderfalls", first.getSeriesName());
		assertEquals("2004-03-12", first.getSeriesInfo().getStartDate().toString());
		assertEquals("Wax Lion", first.getTitle());
		assertEquals("1", first.getEpisode().toString());
		assertEquals("1", first.getSeason().toString());
		assertEquals("1", first.getAbsolute().toString());
		assertEquals("2004-03-12", first.getAirdate().toString());
		assertEquals("296337", first.getId().toString());
	}

	@Test
	public void getEpisodeListMissingInformation() throws Exception {
		List<Episode> list = db.getEpisodeList(wonderfalls, SortOrder.Airdate, Locale.JAPANESE);

		Episode first = list.get(0);

		assertEquals("Wonderfalls", first.getSeriesName());
		assertEquals("Wax Lion", first.getTitle());
	}

	@Test
	public void getEpisodeListIllegalSeries() throws Exception {
		List<Episode> list = db.getEpisodeList(new SearchResult(313193, "*** DOES NOT EXIST ***"), SortOrder.Airdate, Locale.ENGLISH);
		assertTrue(list.isEmpty());
	}

	@Test
	public void getEpisodeListNumberingDVD() throws Exception {
		List<Episode> list = db.getEpisodeList(firefly, SortOrder.DVD, Locale.ENGLISH);

		Episode first = list.get(0);
		assertEquals("Firefly", first.getSeriesName());
		assertEquals("2002-09-20", first.getSeriesInfo().getStartDate().toString());
		assertEquals("Serenity", first.getTitle());
		assertEquals("1", first.getEpisode().toString());
		assertEquals("1", first.getSeason().toString());
		assertEquals("1", first.getAbsolute().toString());
		assertEquals("2002-12-20", first.getAirdate().toString());
	}

	@Test
	public void getEpisodeListNumberingAbsoluteAirdate() throws Exception {
		List<Episode> list = db.getEpisodeList(firefly, SortOrder.AbsoluteAirdate, Locale.ENGLISH);

		Episode first = list.get(0);
		assertEquals("Firefly", first.getSeriesName());
		assertEquals("2002-09-20", first.getSeriesInfo().getStartDate().toString());
		assertEquals("The Train Job", first.getTitle());
		assertEquals("20020920", first.getEpisode().toString());
		assertEquals(null, first.getSeason());
		assertEquals("2", first.getAbsolute().toString());
		assertEquals("2002-09-20", first.getAirdate().toString());
	}

	public void getEpisodeListLink() {
		assertEquals("http://www.thetvdb.com/?tab=seasonall&id=78874", db.getEpisodeListLink(firefly).toString());
	}

	@Test
	public void lookupByID() throws Exception {
		SearchResult series = db.lookupByID(78874, Locale.ENGLISH);
		assertEquals("Firefly", series.getName());
		assertEquals(78874, series.getId());
	}

	@Test
	public void lookupByIMDbID() throws Exception {
		SearchResult series = db.lookupByIMDbID(303461, Locale.ENGLISH);
		assertEquals("Firefly", series.getName());
		assertEquals(78874, series.getId());
	}

	@Test
	public void getSeriesInfo() throws Exception {
		TheTVDBSeriesInfo it = db.getSeriesInfo(80348, Locale.ENGLISH);

		assertEquals(80348, it.getId(), 0);
		assertEquals("Action", it.getGenres().get(0));
		assertEquals("en", it.getLanguage());
		assertEquals("45", it.getRuntime().toString());
		assertEquals("Chuck", it.getName());
		assertEquals(8.7, it.getRating(), 0.5);
		assertEquals(27155, it.getRatingCount(), 1000);
		assertEquals("tt0934814", it.getImdbId());
		assertEquals("Friday", it.getAirsDayOfWeek());
		assertEquals("8:00 PM", it.getAirsTime());
		assertEquals(182, it.getOverview().length(), 20);
		assertEquals("https://thetvdb.com/banners/graphical/80348-g3.jpg", it.getBannerUrl().toString());
	}

	@Test
	public void getArtwork() throws Exception {
		Artwork i = db.getArtwork(buffy.getId(), "fanart", Locale.ENGLISH).get(0);

		assertEquals("[fanart, graphical, 1920x1080]", i.getTags().toString());
		assertEquals("https://thetvdb.com/banners/fanart/original/70327-3.jpg", i.getUrl().toString());
		assertTrue(i.matches("fanart", "1920x1080"));
		assertFalse(i.matches("fanart", "1920x1080", "1"));
		assertEquals(22.0, i.getRating(), 5.0);
	}

	@Test
	public void getLanguages() throws Exception {
		List<String> languages = db.getLanguages();
		assertEquals("[aa, ab, af, ak, am, ar, an, as, av, ae, ay, az, ba, bm, be, bn, bh, bi, bo, bs, br, bg, ca, cs, ch, ce, cu, cv, kw, co, cr, cy, da, de, dv, dz, el, en, eo, et, eu, ee, fo, fa, fj, fi, fr, fy, ff, gd, ga, gl, gv, gn, gu, ht, ha, he, hz, hi, ho, hr, hu, hy, ig, io, ii, iu, ie, ia, id, ik, is, it, jv, ja, kl, kn, ks, ka, kr, kk, km, ki, rw, ky, kv, kg, ko, kj, ku, lo, la, lv, li, ln, lt, lb, lu, lg, mh, ml, mr, mk, mg, mt, mn, mi, ms, my, na, nv, nr, nd, ng, ne, nl, no, ny, oc, oj, or, om, os, pa, pi, pl, pt, pt, ps, qu, rm, ro, rn, ru, sg, sa, si, sk, sl, se, sm, sn, sd, so, st, es, sq, sc, sr, ss, su, sw, sv, ty, ta, tt, te, tg, tl, th, ti, to, tn, ts, tk, tr, tw, ug, uk, ur, uz, ve, vi, vo, wa, wo, xh, yi, yo, za, zh, zu]", languages.toString());
	}

	@Test
	public void getActors() throws Exception {
		Person p = db.getActors(firefly.getId(), Locale.ENGLISH).get(0);
		assertEquals("Nathan Fillion", p.getName());
		assertEquals("Malcolm 'Mal' Reynolds", p.getCharacter());
		assertEquals("Actor", p.getJob());
		assertEquals(null, p.getDepartment());
		assertEquals("1", p.getOrder().toString());
		assertEquals("https://thetvdb.com/banners/actors/62ded51ee72d9.jpg", p.getImage().toString());
	}

	@Test
	public void getEpisodeInfo() throws Exception {
		EpisodeInfo i = db.getEpisodeInfo(296337, Locale.ENGLISH);

		assertEquals("78845", i.getSeriesId().toString());
		assertEquals("296337", i.getId().toString());
		assertEquals(8.2, i.getRating(), 0.1);
		assertEquals(168, i.getVotes(), 20);
		assertEquals("When Jaye Tyler is convinced by a waxed lion to chase after a shinny quarter, she finds herself returning a lost purse to a lady (who instead of thanking her, is punched in the face), meeting an attractive and sweet bartender names Eric, introducing her sister, Sharon to the EPS newly divorced bachelor, Thomas, she knows, and later discovering her sister, Sharon's sexuality.", i.getOverview().toString());
		assertEquals("[Todd Holland]", i.getDirectors().toString());
		assertEquals("[Bryan Fuller]", i.getWriters().toString());
		assertEquals("[Anna Starnino, Bailey Stocker, Brandon Oakes, Chantal Purdy, Corry Karpf, Curt Wu, Gerry Fiorini, Jorge Molina, Kari Matchett, Kathy Greenwood, Kim Roberts, Lisa Marcos, Melissa Grelo, Morgan Drmaj, Neil Grayston, Scotch Ellis Loring, Ted Dykstra]", i.getActors().toString());
	}

}
