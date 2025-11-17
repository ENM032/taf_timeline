package com.timeline.bootstrap;

import com.timeline.model.Fact;
import com.timeline.repository.FactRepository;

import java.time.LocalDate;

public class Seeder {
    public static void seedIfEmpty(FactRepository repo) {
        loadCsv(repo);
        addIfMissing(repo, fact("2024-01-01", "First day of 2024", "New Year celebrations worldwide, kicking off 2024.", "current", "https://en.wikipedia.org/wiki/New_Year"));
        addIfMissing(repo, fact("2004-01-04", "Spirit rover lands on Mars", "NASA's Spirit rover successfully lands on Mars in January 2004.", "history", "https://mars.nasa.gov/mer/home/"));
        addIfMissing(repo, fact("1984-01-24", "Apple Macintosh introduced", "Apple unveils the original Macintosh.", "history", "https://en.wikipedia.org/wiki/Macintosh"));
        addIfMissing(repo, fact("2009-01-20", "Barack Obama inaugurated", "Barack Obama becomes the 44th President of the United States.", "history", "https://en.wikipedia.org/wiki/First_inauguration_of_Barack_Obama"));
        addIfMissing(repo, fact("2004-02-04", "Facebook launches", "Mark Zuckerberg launches Facebook from Harvard.", "history", "https://en.wikipedia.org/wiki/Facebook"));
        addIfMissing(repo, fact("2016-02-11", "Gravitational waves detected", "LIGO announces first direct detection of gravitational waves.", "history", "https://en.wikipedia.org/wiki/Gravitational_waves#Direct_detection"));
        addIfMissing(repo, fact("1961-04-12", "First human in space", "Yuri Gagarin orbits Earth aboard Vostok 1.", "history", "https://en.wikipedia.org/wiki/Yuri_Gagarin"));
        addIfMissing(repo, fact("1990-04-24", "Hubble Space Telescope launched", "Hubble is deployed by Space Shuttle Discovery.", "history", "https://en.wikipedia.org/wiki/Hubble_Space_Telescope"));
        addIfMissing(repo, fact("2007-06-29", "iPhone released", "Apple releases the first iPhone.", "history", "https://en.wikipedia.org/wiki/IPhone_(1st_generation)"));
        addIfMissing(repo, fact("1969-07-20", "Apollo 11 moon landing", "Neil Armstrong and Buzz Aldrin land on the Moon.", "history", "https://en.wikipedia.org/wiki/Apollo_11"));
        addIfMissing(repo, fact("1989-11-09", "Fall of the Berlin Wall", "Borders between East and West Berlin open.", "history", "https://en.wikipedia.org/wiki/Berlin_Wall#Fall"));
        addIfMissing(repo, fact("1945-10-24", "United Nations founded", "The UN Charter comes into force.", "history", "https://en.wikipedia.org/wiki/United_Nations"));
        addIfMissing(repo, fact("2012-07-04", "Higgs boson announced", "CERN announces observation of a new boson consistent with the Higgs.", "history", "https://en.wikipedia.org/wiki/Higgs_boson#Discovery"));
        addIfMissing(repo, fact("1963-08-28", "I Have a Dream", "Martin Luther King Jr. delivers his historic speech in Washington, D.C.", "history", "https://en.wikipedia.org/wiki/I_Have_a_Dream"));
        addIfMissing(repo, fact("1995-08-24", "Windows 95 released", "Microsoft releases Windows 95.", "history", "https://en.wikipedia.org/wiki/Windows_95"));
    }

    private static Fact fact(String date, String title, String summary, String category, String url) {
        Fact f = new Fact();
        f.setEventDate(LocalDate.parse(date));
        f.setTitle(title);
        f.setSummary(summary);
        f.setCategory(category);
        f.setSourceUrl(url);
        return f;
    }

    private static void addIfMissing(FactRepository repo, Fact f) {
        boolean exists = repo.existsByDateAndTitle(f.getEventDate(), f.getTitle());
        if (!exists) repo.add(f);
    }

    private static void loadCsv(FactRepository repo) {
        try {
            java.io.InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("seed/facts.csv");
            if (in == null) return;
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(in));
            String line;
            boolean headerSkipped = false;
            while ((line = reader.readLine()) != null) {
                if (!headerSkipped) { headerSkipped = true; continue; }
                String[] parts = line.split(",");
                if (parts.length < 5) continue;
                String date = parts[0].trim();
                String title = parts[1].trim();
                String summary = parts[2].trim();
                String category = parts[3].trim();
                String url = parts[4].trim();
                Fact f = fact(date, title, summary, category, url);
                addIfMissing(repo, f);
            }
        } catch (Exception e) {
            // swallow errors to avoid startup failure
        }
    }
}