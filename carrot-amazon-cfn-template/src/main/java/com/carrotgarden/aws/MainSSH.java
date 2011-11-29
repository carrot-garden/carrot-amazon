package com.carrotgarden.aws;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class MainSSH {

	static final Logger logger = LoggerFactory.getLogger(MainSSH.class);

	public static void main(String[] args) throws Exception {

		logger.debug("init");

		//

		final JSch jsch = new JSch();

		final String home = System.getProperty("user.home");

		final File file = new File(home,
				".amazon/carrotgarden/keys/carrotgarden.pem");

		logger.debug("file=" + file);

		//

		jsch.addIdentity(file.getAbsolutePath());

		final String username = "ubuntu";
		final String hostname = "builder.carrotgarden.com";
		final int port = 22;

		final Session session = jsch.getSession(username, hostname, port);

		session.setConfig("StrictHostKeyChecking", "no");

		//

		final String repo = "http://extras.ubuntu.com/ubuntu/ oneiric main";

		session.connect();

		exec(session, "sudo ls -las /root");

		exec(session, "sudo apt-get --assume-yes update");

		exec(session, "sudo apt-get --assume-yes upgrade");

		exec(session, "sudo apt-get --assume-yes install"
				+ " mc tar wget zip unzip");

		//

		exec(session, "sudo mkdir --verbose --parents /opt/java32");

		exec(session,
				"cd /opt/java32; sudo wget  --timestamping"
						+ " http://download.oracle.com/otn-pub/java/jdk/7u1-b08/jdk-7u1-linux-i586.tar.gz");

		exec(session,
				"cd /opt/java32; sudo tar --extract --gzip --keep-newer-files --totals"
						+ " --file jdk-7u1-linux-i586.tar.gz");

		exec(session, "sudo update-alternatives --verbose"
				+ " --remove-all java");

		exec(session,
				"sudo update-alternatives --verbose"
						+ " --install /usr/bin/java java /opt/java32/jdk1.7.0_01/bin/java 10");

		exec(session, "java -version 2>&1");

		//

		exec(session, "sudo addgroup --system karaf");
		exec(session, "sudo adduser --system "
				+ " --ingroup karaf --home /var/karaf karaf");
		exec(session, "sudo adduser ubuntu karaf");

		// exec(session, "sudo mkdir --parents /var/karaf");

		// exec(session, "sudo cp --recursive /home/ubuntu/.ssh /var/karaf");

		exec(session,
				"sudo chown --changes --recursive ubuntu:karaf /var/karaf");
		exec(session,
				"sudo chmod --changes --recursive o-rwx,g+rw,g+s,u-s /var/karaf");

		// DIFF=$(expr $(date +%s) - $(stat --format %X /var/lib/apt/lists) )

		// exec(session, "sudo reboot");

		session.disconnect();

		//

		logger.debug("done");

	}

	static void exec(Session session, String command) throws Exception {

		logger.debug("--- " + command);

		final ChannelExec channel = (ChannelExec) session.openChannel("exec");

		channel.setCommand(command);

		channel.connect();

		final InputStream input = channel.getInputStream();

		final Reader reader = new InputStreamReader(input);

		final BufferedReader buffered = new BufferedReader(reader);

		while (true) {

			final String line = buffered.readLine();

			if (line == null) {
				break;
			}

			logger.debug(">>> " + line);

		}

		channel.disconnect();

	}

}
