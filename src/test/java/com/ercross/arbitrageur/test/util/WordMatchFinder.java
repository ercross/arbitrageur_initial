package com.ercross.arbitrageur.test.util;

import com.ercross.arbitrageur.exception.WrongPatternException;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.regex.Pattern;

import static com.ercross.arbitrageur.util.WordMatchFinder.findTeamNamesBestMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WordMatchFinder {

    //The pattern used here is tailored for nairabet website
    @Test
    public void testFindBestMatch() throws WrongPatternException {
        String target = "Burnley-Wolves";
        LocalTime eventTime = LocalTime.of(18, 0);
        String dataPattern = "([A-Za-z0-9 ]+[A-Za-z0-9 ]*)\\sVs\\s([A-Za-z0-9 ]+[A-Za-z0-9 ]*)\\n\\nCode: [0-9]+\\n[0-9]+\\s[A-Za-z]+\\s([0-9]+:[0-9]+)\\n\\+[0-9]+>\\nHome\\n[0-9]+.[0-9]+\\n\\nDraw\\n[0-9]+.[0-9]+\\n\\nAway\\n[0-9]+.[0-9]+\\n\\n"  ;
        Pattern pattern = Pattern.compile(dataPattern);

        String bestMatch = findTeamNamesBestMatch(target, page, pattern, eventTime);

        assertTrue(bestMatch.equals("Burnley Fc-Wolverhampton Wanderers"));
    }

    private String page = "Burnley Fc Vs Wolverhampton Wanderers\n" +
            "\n" +
            "Code: 37816\n" +
            "15 Jul 18:00\n" +
            "+313>\n" +
            "Home\n" +
            "4.85\n" +
            "\n" +
            "Draw\n" +
            "3.30\n" +
            "\n" +
            "Away\n" +
            "1.94\n" +
            "\n" +
            "Manchester City Vs Afc Bournemouth\n" +
            "\n" +
            "Code: 37814\n" +
            "15 Jul 18:00\n" +
            "+316>\n" +
            "Home\n" +
            "1.16\n" +
            "\n" +
            "Draw\n" +
            "9.30\n" +
            "\n" +
            "Away\n" +
            "17.50\n" +
            "\n" +
            "Everton Fc Vs Aston Villa\n" +
            "\n" +
            "Code: 37817\n" +
            "16 Jul 18:00\n" +
            "+311>\n" +
            "Home\n" +
            "2.10\n" +
            "\n" +
            "Draw\n" +
            "3.65\n" +
            "\n" +
            "Away\n" +
            "3.70\n" +
            "\n" +
            "Crystal Palace Vs Manchester United\n" +
            "\n" +
            "Code: 37812\n" +
            "16 Jul 20:15\n" +
            "+329>\n" +
            "Home\n" +
            "10.25\n" +
            "\n" +
            "Draw\n" +
            "5.30\n" +
            "\n" +
            "Away\n" +
            "1.35\n" +
            "\n" +
            "West Ham United Vs Watford Fc\n" +
            "\n" +
            "Code: 37815\n" +
            "17 Jul 20:00\n" +
            "+323>\n" +
            "Home\n" +
            "2.45\n" +
            "\n" +
            "Draw\n" +
            "3.05\n" +
            "\n" +
            "Away\n" +
            "3.45\n" +
            "\n" +
            "Afc Bournemouth Vs Southampton Fc\n" +
            "\n" +
            "Code: 84433\n" +
            "19 Jul 14:00\n" +
            "+303>\n" +
            "Home\n" +
            "2.35\n" +
            "\n" +
            "Draw\n" +
            "3.70\n" +
            "\n" +
            "Away\n" +
            "3.05\n" +
            "\n" +
            "Brighton & Hove Albion Vs Newcastle United\n" +
            "\n" +
            "Code: 84436\n" +
            "20 Jul 18:00\n" +
            "+286>\n" +
            "Home\n" +
            "1.94\n" +
            "\n" +
            "Draw\n" +
            "3.55\n" +
            "\n" +
            "Away\n" +
            "4.40\n" +
            "\n" +
            "Wolverhampton Wanderers Vs Crystal Palace\n" +
            "\n" +
            "Code: 84468\n" +
            "20 Jul 20:15\n" +
            "+279>\n" +
            "Home\n" +
            "1.50\n" +
            "\n" +
            "Draw\n" +
            "4.25\n" +
            "\n" +
            "Away\n" +
            "8.00\n" +
            "\n" +
            "Aston Villa Vs Arsenal Fc\n" +
            "\n" +
            "Code: 84430\n" +
            "21 Jul 20:15\n" +
            "+283>\n" +
            "Home\n" +
            "3.75\n" +
            "\n" +
            "Draw\n" +
            "3.90\n" +
            "\n" +
            "Away\n" +
            "2.00\n" +
            "\n" +
            "Newcastle United Vs Tottenham Hotspur\n" +
            "\n" +
            "Code: 37820\n" +
            "15 Jul 18:00\n" +
            "+311>\n" +
            "Home\n" +
            "5.20\n" +
            "\n" +
            "Draw\n" +
            "4.05\n" +
            "\n" +
            "Away\n" +
            "1.70\n" +
            "\n" +
            "Arsenal Fc Vs Liverpool Fc\n" +
            "\n" +
            "Code: 37811\n" +
            "15 Jul 20:15\n" +
            "+304>\n" +
            "Home\n" +
            "4.00\n" +
            "\n" +
            "Draw\n" +
            "4.05\n" +
            "\n" +
            "Away\n" +
            "1.90\n" +
            "\n" +
            "Leicester City Vs Sheffield United\n" +
            "\n" +
            "Code: 37818\n" +
            "16 Jul 18:00\n" +
            "+297>\n" +
            "Home\n" +
            "2.10\n" +
            "\n" +
            "Draw\n" +
            "3.40\n" +
            "\n" +
            "Away\n" +
            "4.00\n" +
            "\n" +
            "Southampton Fc Vs Brighton & Hove Albion\n" +
            "\n" +
            "Code: 37813\n" +
            "16 Jul 20:15\n" +
            "+313>\n" +
            "Home\n" +
            "2.30\n" +
            "\n" +
            "Draw\n" +
            "3.40\n" +
            "\n" +
            "Away\n" +
            "3.40\n" +
            "\n" +
            "Norwich City Vs Burnley Fc\n" +
            "\n" +
            "Code: 84470\n" +
            "18 Jul 17:30\n" +
            "+251>\n" +
            "Home\n" +
            "3.35\n" +
            "\n" +
            "Draw\n" +
            "3.40\n" +
            "\n" +
            "Away\n" +
            "2.35\n" +
            "\n" +
            "Tottenham Hotspur Vs Leicester City\n" +
            "\n" +
            "Code: 84472\n" +
            "19 Jul 16:00\n" +
            "+277>\n" +
            "Home\n" +
            "2.35\n" +
            "\n" +
            "Draw\n" +
            "3.50\n" +
            "\n" +
            "Away\n" +
            "3.20\n" +
            "\n" +
            "Sheffield United Vs Everton Fc\n" +
            "\n" +
            "Code: 84475\n" +
            "20 Jul 18:00\n" +
            "+259>\n" +
            "Home\n" +
            "2.30\n" +
            "\n" +
            "Draw\n" +
            "3.30\n" +
            "\n" +
            "Away\n" +
            "3.50\n" +
            "\n" +
            "Watford Fc Vs Manchester City\n" +
            "\n" +
            "Code: 84474\n" +
            "21 Jul 18:00\n" +
            "+256>\n" +
            "Home\n" +
            "10.25\n" +
            "\n" +
            "Draw\n" +
            "6.50\n" +
            "\n" +
            "Away\n" +
            "1.29\n" +
            "\n" +
            "Manchester United Vs West Ham United\n" +
            "\n" +
            "Code: 84432\n" +
            "22 Jul 18:00\n" +
            "+273>\n" +
            "Home\n" +
            "1.30\n" +
            "\n" +
            "Draw\n" +
            "6.20\n" +
            "\n" +
            "Away\n" +
            "10.50";
}
