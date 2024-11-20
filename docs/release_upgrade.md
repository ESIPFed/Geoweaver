# How to Create a New Release in Geoweaver

To upgrade Geoweaver to a new version, follow these steps to update the necessary files and manage the release on GitHub.

1. **Update Version in Files**
    - Update the version number in both `gw.js` and `pom.xml` to the latest version (e.g., from `1.5.1` to `1.5.2`).

2. **Remove Latest Tag**
    - Navigate to the relevant directory to check out the latest master branch.
        ```bash
        mkdir -p ../rmlatest
        cd ../rmlatest
        git clone https://github.com/ESIPFed/Geoweaver.git
        cd Geoweaver
        ```
    - Remove the `latest` tag from the remote repository.
        ```bash
        git push --delete origin latest
        ```
    This step ensures that the `latest` tag is freed up for the new release.

3. **Update Draft Release Tag**
    - Go to the [Geoweaver releases page](https://github.com/ESIPFed/Geoweaver/releases).
    - You will see that the old version (e.g., `v1.5.1`) is tagged as a draft because its tag was deleted.
    - Click on "Edit" and change the tag back to its original format, for example, `v1.5.1-pre`.
    - Publish these changes. This allows for proper version tracking while preparing the new release.

4. **Push File Changes**
    - Commit and push the changes made to `gw.js` and `pom.xml`.
    - Note: This action might trigger a workflow, which you can cancel if it’s not needed at this point.

5. **Draft a New Release**
    - On GitHub release page, draft a new release.
    - Choose a new tag version (e.g., `v1.5.2-pre`) and select the previous tag version as the target (e.g., `v1.5.1`).
    - Click button to automatically generate the release notes, and then publish the release.
    - Wait for the `release_workflow` in the GitHub Action to complete.

6. **Update Release Tag to Latest**
    - Once the release workflow is finished, you should see the installers and JAR files are attached on the new release page as artifacts. Update the release tag (e.g., `v1.5.2-pre`) back to `latest`. This ensures that PyGeoweaver and other installers will download the latest jar.
    - Click "Update release" to save the changes.
    - Note: Click `+ create new tag:latest on publish` in the new version (e.g., v1.5.2) if tag latest was not identified during selection. The badge `latest` besides version name is not a tag name.

Once all these steps are completed, the new version of Geoweaver should be published, and available to be pulled by either the URL or the [`pygeoweaver`](https://github.com/ESIPFed/pygeoweaver) library.
