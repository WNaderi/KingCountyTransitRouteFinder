import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;


public class RouteFinder implements IRouteFinder {

    //regex for finding the route ID's
    private static final Pattern ID_REGEX = Pattern.compile("/route/(\\d+-?\\d*)");


    public Map<String, Map<String, String>> getBusRoutesUrls(final char destInitial) {

        Map<String, Map<String, String>> ans = new HashMap<>();

        try {

            URLConnection transurl = new URL(IRouteFinder.TRANSIT_WEB_URL).openConnection();
            transurl.setRequestProperty("user-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            BufferedReader in = new BufferedReader(new InputStreamReader(transurl.getInputStream()));
            
            String inputLine = "";

            String text = "";
            while ((inputLine = in.readLine()) != null) {
                text += inputLine + "\n"; 
            }


            in.close();

            //1) find destinations that start with destInitial and make them KEY of our return.
                ArrayList<String> dests = getDestinationString(text, destInitial);
                

            //2) find the route ID & link & insert them with the destinations
                for (int i = 0; i < dests.size(); i++) {
                    ans.put(dests.get(i), getIDandURL(text, dests.get(i)));
                }

            
        } catch (Exception eo) {

            System.out.println(eo.getMessage());

        }

        return ans;

    }

    public Map<String, List<Long>> getBusRouteTripsLengthsInMinutesToAndFromDestination(final Map<String, String> destinationBusesMap) {

        Map<String, List<Long>> ans = new HashMap<>();
        
        Set<String> ourset = destinationBusesMap.keySet();


        //For each key in destinationBusesMap find the time difference and put it into a list. Take the list and put it into the map we will return.
        for (String s : ourset) {

            try {

                URLConnection transurl = new URL(destinationBusesMap.get(s)).openConnection();
                transurl.setRequestProperty("user-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
                BufferedReader in = new BufferedReader(new InputStreamReader(transurl.getInputStream()));
                String inputLine = "";

                String text = "";
                while ((inputLine = in.readLine()) != null) {
                    text += inputLine + "\n"; 
                }
                List<Long> timedifference = getTimesDifference(text);
                ans.put(s + " -", timedifference);



            } catch (Exception e) {

                System.out.println(e.getMessage());

            }

        }

        return ans;

    }
    //Helper method for locating the times and calculating the time difference in minutes
    private List<Long> getTimesDifference(String text) {

        List<Long> ans = new ArrayList<Long>();


        //regex for first & last entry in a table row 
        Pattern pattern = Pattern.compile("<tr>\\s*(<td class=\"text-center\">\\d{3}</td>\\s*)*(<td class=\"text-center\">----</td>\\s*)*<td class=\"text-center\">\\s*(\\d?\\d:\\d{2} [A|P]M)\\s*</td>");
        Pattern pattern2 = Pattern.compile("(\\d?\\d:\\d{2} [A|P]M)\\s*(<a href.*?>e</a>\\s*)*</td>\\s*</tr>");
        Matcher time1 = pattern.matcher(text);
        Matcher time2 = pattern2.matcher(text);
        SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
        while (time1.find() && time2.find()) {
            try {
            Date date1 = format.parse(time1.group(3));
            Date date2 = format.parse(time2.group(1));
            ans.add(Math.abs((date2.getTime() - date1.getTime()) / 60000));

            } catch (ParseException pe) {

                System.out.println("Parse Exception u dum dum");

            }

        }

        return ans;

    }

    //Helper method for finding the destination names
    private ArrayList<String> getDestinationString(String text, final char destInitial) {
        
        
        char destuppercase = checkChar(destInitial);

        ArrayList<String> thedestinations = new ArrayList<>();

        Pattern pattern = Pattern.compile("<h3>(" + destuppercase + ".*?)</h3>");
        Matcher matcher = pattern.matcher(text);

        while(matcher.find()) {

            thedestinations.add(matcher.group(1));

        }

        return thedestinations;

    }

    //Helper for finding the ID's and URL for the ids
    private Map<String, String> getIDandURL(String text, String ourdest) {

        Map<String, String> ans = new HashMap<>();

        Scanner in = new Scanner(text);
        String temp = "";

        //Traverse the html text to the city we are looking at
        while((temp = in.nextLine()) != null) {
            Pattern pattern = Pattern.compile("<h3>" + ourdest + "</h3>");
            Matcher matcher = pattern.matcher(temp);
            if (matcher.find()) 
                break;
        }
        //Get every bus line and put them into the map.
        while ((temp = in.nextLine()) != null) {

                Matcher matcher = ID_REGEX.matcher(temp);

                Pattern patterntemp = Pattern.compile("<hr id="); 
                Matcher matcher2 = patterntemp.matcher(temp);

                if (matcher2.find())
                    break;
                while (matcher.find()) {

                    //id.add(matcher.group(1));
                    ans.put(matcher.group(1), "https://www.communitytransit.org/busservice/schedules/route/" + matcher.group(1));


                }

        }

        return ans;

    }

    //make the destination char we are given into a upper case char if it is not.
    private char checkChar(final char destInitial) {
        
        char ans = destInitial;

        try {

        if (destInitial < 65) {

            throw new RuntimeException();

        }

        if (destInitial > 90 && destInitial < 97) {
            throw new RuntimeException();

        }

        //if lowercase make uppercase
        if (destInitial >= 97 && destInitial <= 122) {
    
            ans = (char) (destInitial - 32);

        }

        } catch (Exception ex) {

            System.out.println(ex.getMessage());

        }

        return ans;

    }

}

