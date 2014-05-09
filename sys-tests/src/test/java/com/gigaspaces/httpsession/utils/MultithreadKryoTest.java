package com.gigaspaces.httpsession.utils;

import org.junit.Before;
import org.junit.Test;

import com.esotericsoftware.kryo.KryoException;
import com.gigaspaces.httpsession.models.Person;
import com.gigaspaces.httpsession.serialize.CompressUtils;
import com.gigaspaces.httpsession.serialize.KryoSerializerImpl;
import com.gigaspaces.httpsession.serialize.NonCompressCompressor;
import com.gigaspaces.httpsession.serialize.SerializeUtils;

public class MultithreadKryoTest {

	private Thread writer;
	private Thread reader;

	private String clazz = Person.class.getName();
	private Object actual = new Person();
	private byte[] write_slot;
	private Object read_slot;

	@Before
	public void start() {

		writer = new Thread(new Runnable() {

			public void run() {

				ClassLoader loader = new ClassLoader() {
					@Override
					public Class<?> loadClass(String name)
							throws ClassNotFoundException {
						
						if (clazz.equals(name))
							return Person.class;

						return super.loadClass(name);
					}
				};			

				KryoSerializerImpl serializer = new KryoSerializerImpl();
				serializer.setClassLoader(loader);
				CompressUtils.register(new NonCompressCompressor());
				SerializeUtils.register(serializer);

				write_slot = serializer.serialize(actual);

				//notifyAll();
			}
		});

		reader = new Thread(new Runnable() {

			public void run() {	
				ClassLoader loader = new ClassLoader() {
					@Override
					public Class<?> loadClass(String name)
							throws ClassNotFoundException {
						
						if (clazz.equals(name))
							return null;

						return super.loadClass(name);
					}
				};
				
				KryoSerializerImpl serializer = new KryoSerializerImpl();
				serializer.setClassLoader(loader);
				CompressUtils.register(new NonCompressCompressor());
				SerializeUtils.register(serializer);

				read_slot = serializer.deserialize(write_slot);

				notifyAll();

			}
		});
	}

	@Test(expected = KryoException.class)
	public void test() throws InterruptedException {
		
		writer.start();
		synchronized (writer) {
			writer.wait();
		}

		reader.start();

		synchronized (reader) {
			reader.wait();
		}
		
	}
}
