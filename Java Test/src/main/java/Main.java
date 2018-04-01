
import com.troy.serialization.io.NativeOutput;
import com.troy.serialization.serializers.FieldSerializer;
import com.troy.serialization.util.InternalLog;
import com.troy.serialization.util.SerializationUtils;
import com.troy.serialization.util.StringFormatter;

public class Main {

	public static void main(String[] args) throws Throwable {
		new FieldSerializer<TestSub>(TestSub.class);
		System.exit(0);
		
		SerializationUtils.init();
		NativeOutput o = new NativeOutput();
		o.writeInts(new int[] { 0x12, 0x324, 0x432, 0x123, 0x324 });
		System.out.println(StringFormatter.toHexString(o.toByteArray()));
		o.close();
		InternalLog.dumpToOut();
	}
}