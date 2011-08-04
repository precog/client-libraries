import format.swf.Reader;
import format.swf.Writer;
import neko.FileSystem;
import neko.io.File;
import neko.Lib;
import neko.Sys;
import haxe.io.BytesInput;
import haxe.io.BytesOutput;
import format.swf.Data;
import format.swf.Constants;
class ScriptLimit 
{
	public static inline var MAX_VALUE = 65000;
	public static function main()
	{
		var args = Sys.args();
		if (args.length < 1 || args.length > 2)
		{
			trace("invalid number of arguments");
			return;
		}
		var src = args[0],
			dst = args.length == 2 ? args[1] : src,
			swfBytesInput = new BytesInput(File.getBytes(src)),
			swfReader = new Reader(swfBytesInput),
			header = swfReader.readHeader(),
			tags = swfReader.readTagList();
		swfBytesInput.close();

		var data = new BytesOutput();
		data.bigEndian = false;
		data.writeUInt16(MAX_VALUE);//maxRecursionDepth
		data.writeUInt16(MAX_VALUE);//scriptTimeoutSeconds
		
		var found = false;
		for (i in 0...tags.length)
		{
			switch(tags[i])
			{
				case TUnknown(id, _):
					if (id != TagId.ScriptLimits) continue;
					tags[i] = TUnknown(TagId.ScriptLimits, data.getBytes());
					found = true;
					break;
				default: // do nothing
			}
		}
		if(!found)
			tags.unshift(TUnknown(TagId.ScriptLimits, data.getBytes()));

		var swfBytesOutput = new BytesOutput();
		new format.swf.Writer(swfBytesOutput).write({header:header, tags:tags});

		var fout = File.write(dst, true);//output swf
		fout.write(swfBytesOutput.getBytes());
		fout.close();
	}
}