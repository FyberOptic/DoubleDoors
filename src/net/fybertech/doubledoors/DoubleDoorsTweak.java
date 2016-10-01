package net.fybertech.doubledoors;

import java.io.File;
import java.util.List;

import net.fybertech.meddle.MeddleMod;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

@MeddleMod(depends={"dynamicmappings", "meddleapi"}, id = "doubledoors", author="FyberOptic", name="DoubleDoors", version="1.0.4")
public class DoubleDoorsTweak implements ITweaker {

	@Override
	public void acceptOptions(List<String> arg0, File arg1, File arg2, String arg3) {
	}

	@Override
	public String[] getLaunchArguments() {
		return new String[0];
	}

	@Override
	public String getLaunchTarget() {
		return null;
	}

	@Override
	public void injectIntoClassLoader(LaunchClassLoader arg0) {
		arg0.registerTransformer(DoubleDoorsTransformer.class.getName());
	}

}
