import unittest

from release import common_dependency_urls, release_selection


class ReleaseSelectionTest(unittest.TestCase):
    versions = {"proto-google-common-protos": "2.78.0-0", "pgv-proto": "1.3.3-0"}

    def test_master_publishes_snapshots(self):
        self.assertIsNone(release_selection("refs/heads/master", self.versions))

    def test_matching_tag_selects_only_its_library(self):
        self.assertEqual(
            release_selection("refs/tags/pgv-proto/1.3.3-0", self.versions), "pgv-proto"
        )

    def test_wrong_versions_unknown_libraries_and_branches_are_rejected(self):
        for ref in (
            "refs/tags/pgv-proto/1.3.2-0",
            "refs/tags/pgv-proto/1.3.3-SNAPSHOT",
            "refs/tags/v1.3.3",
            "refs/tags/unknown/1.3.3-0",
            "refs/heads/feature",
            "refs/pull/1/merge",
            "refs/tags/pgv-proto/1.3.3-0/extra",
        ):
            with self.subTest(ref=ref), self.assertRaises(ValueError):
                release_selection(ref, self.versions)

    def test_dependencies_must_be_published_for_every_supported_variant(self):
        urls = common_dependency_urls("2.78.0-0")
        self.assertEqual(len(set(urls)), 6)
        self.assertTrue(any("scalapb_0.11_2.12/" in url for url in urls))
        self.assertTrue(any("scalapb_1.0_3/" in url for url in urls))
        self.assertTrue(all(url.endswith("-2.78.0-0.pom") for url in urls))
