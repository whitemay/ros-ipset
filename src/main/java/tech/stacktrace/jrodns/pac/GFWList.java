package tech.stacktrace.jrodns.pac;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * @author yinqiwen
 */
public class GFWList {

    static Logger logger = LoggerFactory.getLogger(GFWList.class);

    private static GFWList instance = new GFWList();

    private ArrayList<GFWListRule> whiteList = new ArrayList<>();
    private ArrayList<GFWListRule> blackList = new ArrayList<>();

    private GFWList() {
    }

    public static GFWList getInstacne() {
        return instance;
    }

    public boolean match(String host) {
        for (GFWListRule rule : whiteList) {
            if (rule.match(host)) {
                return false;
            }
        }
        for (GFWListRule rule : blackList) {
            if (rule.match(host)) {
                return true;
            }
        }
        return whiteList.isEmpty() && blackList.isEmpty();
    }

    public static void loadRules(String rules) throws IOException {
        ArrayList<GFWListRule> w = new ArrayList<>();
        ArrayList<GFWListRule> b = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new StringReader(rules));
        int i = 0;
        while (true) {
            String line = reader.readLine();
            i++;
            if (i == 1) {
                continue;
            }
            if (StringUtils.isBlank(line)) {
                break;
            }
            line = line.trim();
            if (line.startsWith("!")) {
                continue;
            }
            if (line.startsWith("@@||")) {
                HostUrlWildcardRule rule = new HostUrlWildcardRule();
                rule.init(line.substring(4));
                w.add(rule);
            } else if (line.startsWith("||")) {
                // ||domain
                HostUrlWildcardRule rule = new HostUrlWildcardRule();
                rule.init(line.substring(2));
                b.add(rule);
            } else if (line.startsWith(".")) {
                // .domain
                HostUrlWildcardRule rule = new HostUrlWildcardRule();
                rule.init(line.substring(1));
                b.add(rule);
            } else if (line.startsWith("|http")) {
                UrlWildcardRule rule = new UrlWildcardRule();
                rule.init(line.substring(1));
                b.add(rule);
            } else if (line.startsWith("/") && line.endsWith("/")) {
                UrlRegexRule rule = new UrlRegexRule();
                rule.init(line.substring(1, line.length() - 1));
                b.add(rule);
            } else {
                HostUrlWildcardRule rule = new HostUrlWildcardRule();
                rule.onlyHttp = true;
                rule.init(line);
                b.add(rule);
            }
        }
        instance.blackList = b;
        instance.whiteList = w;

        logger.info("loaded {} blackList records, {} whiteList records", b.size(), w.size());
        logger.info("HostUrlWildcardRule count:{}", HostUrlWildcardRule.count);
        logger.info("UrlRegexRule count:{}", UrlRegexRule.count);
        logger.info("UrlWildcardRule count:{}", UrlWildcardRule.count);
    }

}