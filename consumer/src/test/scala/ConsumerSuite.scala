import com.google.`type`.date.Date
import com.google.pubsub.v1.pubsub.Topic
import com.google.cloud.dataproc.v1.clusters.Cluster
import com.google.protobuf.{ByteString, CodedOutputStream}
import java.io.ByteArrayOutputStream

class ConsumerSuite extends munit.FunSuite {
  test("a consumer generates and serializes messages importing all four libraries") {
    val message = smoke.Envelope(
      date = Some(Date(year = 2026, month = 10, day = 10)),
      topic = Some(Topic(name = "projects/example/topics/smoke")),
      cluster = Some(Cluster(projectId = "example", clusterName = "smoke")),
      label = "valid"
    )
    assertEquals(smoke.Envelope.parseFrom(message.toByteArray), message)
    val options = smoke.Envelope.scalaDescriptor.findFieldByName("label").get.getOptions
    val rules = options.extension(io.envoyproxy.pgv.validate.validate.ValidateProto.rules).get
    assertEquals(rules.getString.minLen, Some(1L))
  }

  test("Google common, Pub/Sub, and Dataproc Java conversions round trip") {
    val date = Date(year = 2026, month = 10, day = 10)
    val topic = Topic(name = "projects/example/topics/smoke")
    val cluster = Cluster(projectId = "example", clusterName = "smoke")
    assertEquals(Date.fromJavaProto(Date.toJavaProto(date)), date)
    assertEquals(Topic.fromJavaProto(Topic.toJavaProto(topic)), topic)
    assertEquals(Cluster.fromJavaProto(Cluster.toJavaProto(cluster)), cluster)
  }

  test("PGV Java conversions round trip") {
    val rules = io.envoyproxy.pgv.validate.validate.StringRules(minLen = Some(1L))
    assertEquals(io.envoyproxy.pgv.validate.validate.StringRules.fromJavaProto(
      io.envoyproxy.pgv.validate.validate.StringRules.toJavaProto(rules)), rules)
  }

  test("unknown fields survive Scala and Java wire round trips") {
    val output = new ByteArrayOutputStream
    val coded = CodedOutputStream.newInstance(output)
    coded.writeRawBytes(Date(year = 2026).toByteArray)
    coded.writeBytes(999, ByteString.copyFromUtf8("future field"))
    coded.flush()
    val parsed = Date.parseFrom(output.toByteArray)
    assert(parsed.unknownFields.serializedSize > 0)
    assertEquals(Date.parseFrom(parsed.toByteArray), parsed)
    assertEquals(Date.parseFrom(com.google.`type`.Date.parseFrom(parsed.toByteArray).toByteArray), parsed)
  }
}
