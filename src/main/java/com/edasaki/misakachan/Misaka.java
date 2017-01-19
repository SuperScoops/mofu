package com.edasaki.misakachan;

import com.edasaki.misakachan.gui.GuiManager;
import com.edasaki.misakachan.persistence.OptionManager;
import com.edasaki.misakachan.persistence.PersistenceManager;
import com.edasaki.misakachan.source.AbstractSource;
import com.edasaki.misakachan.source.english.MangaHere;
import com.edasaki.misakachan.source.test.TestSource;
import com.edasaki.misakachan.spark.SparkManager;
import com.edasaki.misakachan.version.Version;

public class Misaka {

    private static final AbstractSource SOURCES[] = {
            new MangaHere(),
            new TestSource(),
    };

    private static Misaka instance;

    public GuiManager gui;
    public SparkManager spark;
    public PersistenceManager persist;
    public OptionManager options;

    protected static void initialize(Version version) {
        Misaka m = new Misaka();
        instance = m;
        m.init();
    }

    private void init() {
        this.gui = new GuiManager();
        this.gui.showFrame();

        this.persist = new PersistenceManager();
        this.persist.loadFiles();

        this.options = new OptionManager();

        this.updateStatus("Initializing webserver...");

        // Do this last, since we want to be sure everything else is ready
        this.spark = new SparkManager(SOURCES);
        this.spark.startWebsever();
        this.updateStatus("Webserver successfully started.");
        this.updateStatus("Now listening on 0.0.0.0:" + this.spark.getPort() + "...");
    }

    public static Misaka getInstance() {
        return instance;
    }

    public static Misaka instance() {
        return instance;
    }

    public static void update(String s) {
        if (instance != null)
            instance.updateStatus(s);
    }

    public static void error(String s) {
        update("[ERROR] " + s);
    }

    public void updateStatus(String s) {
        this.gui.updateStatus(s);
    }
}