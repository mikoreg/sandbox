import com.example.powernapwatch.core.*;
import java.io.*;
import java.nio.file.*;

public class ReplayCsvTest {
  public static void main(String[] args) throws Exception {
    if (args.length != 1) throw new IllegalArgumentException("csv");
    final long[] onset={0}; final double[] conf={0};
    SleepOnsetDetector d=new SleepOnsetDetector(new SleepOnsetDetector.Config(), new SleepOnsetDetector.Listener(){
      public void onSnapshot(DetectorSnapshot s) {}
      public void onSleepConfirmed(long o,double c){onset[0]=o; conf[0]=c;}
    });
    boolean started=false;
    try(BufferedReader br=Files.newBufferedReader(Path.of(args[0]))) {
      String line=br.readLine();
      while((line=br.readLine())!=null){
        String[] a=line.split(",",16);
        if(a.length<16) continue;
        String type=a[1]; long t=Long.parseLong(a[2]);
        if(!started){d.start(t); started=true;}
        switch(type){
          case "ACC" -> d.onAcceleration(t,Double.parseDouble(a[5]),Double.parseDouble(a[6]),Double.parseDouble(a[7]));
          case "HR" -> { if(!a[8].isEmpty()) d.onHeartRate(t,Double.parseDouble(a[8])); }
          case "IBI" -> { if(!a[9].isEmpty()) d.onIbi(t,Double.parseDouble(a[9])); }
          case "EVENT" -> { if(a[15].contains("onBody=true")) d.onBodyState(true,t); else if(a[15].contains("onBody=false")) d.onBodyState(false,t); }
        }
      }
    }
    System.out.println("onset="+onset[0]+" confidence="+conf[0]);
  }
}
