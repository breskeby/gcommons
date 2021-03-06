package com.goldin.gcommons.beans

import com.goldin.gcommons.BaseTest
import groovy.io.FileType
import org.apache.tools.zip.ZipFile
import org.junit.Test

/**
 * {@link com.goldin.gcommons.beans.FileBean} tests
 */
class FileBeanTest extends BaseTest
{

    private static File writeFile( File f, String content = null )
    {
        assert ( f.getParentFile().isDirectory() || f.getParentFile().mkdirs())

        if ( content )
        {
            f.write( content )
        }
        else
        {
            f.write( f.canonicalPath )
            f.append( System.currentTimeMillis())
            f.append( new Date())
        }

        assert f.exists() && f.isFile()
        f
    }


    @Test
    void shouldDeleteFiles()
    {
        def file = fileBean.tempFile()

        assert file.exists() && file.isFile()

        fileBean.delete( file )

        assert ! file.exists()
        assert ! file.isFile()

        def dir = fileBean.tempDirectory()
        fileBean.delete( writeFile( new File( dir, '1.txt' )))
        fileBean.delete( writeFile( new File( dir, '2.xml' )))
        fileBean.delete( writeFile( new File( dir, '3.ppt' )))

        assert ! dir.list()
        assert ! dir.listFiles()
        assert ! file.exists()
        assert ! file.isFile()

        fileBean.delete( dir )
    }


    @Test
    void shouldMkdir()
    {
        def f = { String name -> new File( constantsBean.USER_HOME_FILE, name ) }

        fileBean.mkdirs( f( 'aa' ), f( 'aa/bb' ), f( 'aa/bb/dd' ), f( 'ee/bb/dd' ), f( 'ff/bb/dd/kk' ))
        verifyBean.directory( f( 'aa' ), f( 'aa/bb' ), f( 'aa/bb/dd' ),
                              f( 'ee' ), f( 'ee/bb' ), f( 'ee/bb/dd' ),
                              f( 'ff' ), f( 'ff/bb' ), f( 'ff/bb/dd' ),f( 'ff/bb/dd/kk' ))

        shouldFailAssert { verifyBean.directory( f( 'aa' ), f( 'aa/bb' ), f( 'aa/bb/dd1' )) }

        f( 'aa/1.txt' ).write( System.currentTimeMillis() as String )
        f( 'aa/bb/2.txt' ).write( System.currentTimeMillis() as String )
        f( 'aa/bb/dd/3.txt' ).write( System.currentTimeMillis() as String )

        f( 'ee/1.txt' ).write( System.currentTimeMillis() as String )
        f( 'ee/bb/2.txt' ).write( System.currentTimeMillis() as String )
        f( 'ee/bb/dd/3.txt' ).write( System.currentTimeMillis() as String )

        f( 'ff/1.txt' ).write( System.currentTimeMillis() as String )
        f( 'ff/bb/2.txt' ).write( System.currentTimeMillis() as String )
        f( 'ff/bb/dd/3.txt' ).write( System.currentTimeMillis() as String )
        f( 'ff/bb/dd/kk/4.txt' ).write( System.currentTimeMillis() as String )

        fileBean.delete( f( 'aa' ), f( 'ee' ), f( 'ff' ))

        shouldFailAssert { verifyBean.directory( f( 'aa' )) }
        shouldFailAssert { verifyBean.directory( f( 'ee' )) }
        shouldFailAssert { verifyBean.directory( f( 'ff' )) }
    }


    @Test
    void shouldDeleteDirectories()
    {
        def dir = fileBean.tempDirectory()

        assert dir.exists() && dir.isDirectory()

        writeFile( new File( dir, '1.txt' ))
        writeFile( new File( dir, '2.xml' ))
        writeFile( new File( dir, '3.ppt' ))
        writeFile( new File( dir, 'a/b/1.txt' ))
        writeFile( new File( dir, 'c/d/2.xml' ))
        writeFile( new File( dir, 'e/f/g/h/3.ppt' ))
        writeFile( new File( dir, '11.txt' ))
        writeFile( new File( dir, '22.xml' ))
        writeFile( new File( dir, '33.ppt' ))
        writeFile( new File( dir, 'aw/bq/1j.txt' ))
        writeFile( new File( dir, 'cy/do/2p.xml' ))
        writeFile( new File( dir, 'easdf/fdsd/gwqeq/hujy/3weqw.ppt.eqeq' ))

        fileBean.delete( dir )
        assert ! dir.exists()
        assert ! dir.isFile()
    }


    @Test
    void shouldCalculateChecksum()
    {
        def file = testResource( 'apache-maven-3.0.1.zip' )
        
        assert fileBean.checksum( file )        == fileBean.checksum( file, 'SHA-1' )
        assert fileBean.checksum( file )        == '7db54443784f547a36a7adb293bfeca2d2c9d15c'
        assert fileBean.checksum( file, 'MD5' ) == '3aeeb8b545ae1b6aa8b2015dce24eec7'

        def dir = fileBean.tempDirectory()
        file    = new File( dir, '1.txt' )

        shouldFailAssert { fileBean.checksum( dir  ) }
        shouldFailAssert { fileBean.checksum( null ) }

        writeFile( file, '7db54443784f547a36a7adb293bfeca2d2c9d15c\r\n' )
        assert fileBean.checksum( file, 'MD5' ) == '04ce83c072936118922107babdf6d21a'
        assert fileBean.checksum( file )        == 'fcd551a840d37d3c885db298e893ec77468a81cd'
        assert fileBean.checksum( file, 'MD5' ) == fileBean.checksum( file, 'MD5' )
        assert fileBean.checksum( file, 'MD5' ) != fileBean.checksum( file )

        fileBean.delete( dir )
    }


    @Test
    void testFiles()
    {
        def    allFiles = fileBean.files( constantsBean.USER_DIR_FILE )
        assert allFiles
        assert allFiles.each{ verifyBean.exists( it ) }
        assert allFiles == fileBean.files( constantsBean.USER_DIR_FILE, null, null, true, false )
        assert allFiles != fileBean.files( constantsBean.USER_DIR_FILE, null, null, true, true  )

        def buildDir   = new File( constantsBean.USER_DIR_FILE, 'build' )
        def classFiles = fileBean.files( buildDir, ['**/*.class'] )
        def sources    = fileBean.files( constantsBean.USER_DIR_FILE, ['**/*.groovy'] )
        assert classFiles.every{ it.name.endsWith( '.class'  ) }
        assert sources.every{    it.name.endsWith( '.groovy' ) }
        verifyBean.file( classFiles as File[] )
        verifyBean.file( sources    as File[] )
        assert classFiles.size() > sources.size()

        shouldFailAssert { fileBean.files( buildDir, ['**/*.ppt'] )}
        assert fileBean.files( buildDir, ['**/*.ppt'], null, true, false, false ).isEmpty()

        allFiles = fileBean.files( constantsBean.USER_DIR_FILE, ['**/*.groovy','**/*.class'], ['**/*Test*.*'] )
        assert ! allFiles.any { it.name.contains( 'Test' ) }
        assert allFiles.every { it.name.endsWith( '.groovy' ) || it.name.endsWith( '.class' ) }

        allFiles.findAll{ it.name.endsWith( '.groovy') }.each {
            File groovyFile ->
            assert allFiles.findAll { it.name == groovyFile.name.replace( '.groovy', '.class' ) }.size() < 3 //  1 or 2, Compiled by Gradle or IDEA
        }
    }


    @Test
    void shouldPack()
    {
        Map testArchives = testArchives()

        for ( archiveName in testArchives.keySet())
        {
            def unpackDir    = testDir( 'unpack' )
            def packDir      = testDir( 'pack' )
            def jarDir       = testDir( 'jar'   )
            def tarDir       = testDir( 'tar'   )
            def tgzDir       = testDir( 'tgz'   )
            def zipDir       = testDir( 'zip'   )
            def tarGzDir     = testDir( 'targz' )

            fileBean.unpack( testResource( "${archiveName}.jar" ), unpackDir )

            fileBean.pack( unpackDir, new File( packDir, "${archiveName}.jar"    ))
            fileBean.pack( unpackDir, new File( packDir, "${archiveName}.tar"    ))
            fileBean.pack( unpackDir, new File( packDir, "${archiveName}.tgz"    ))
            fileBean.pack( unpackDir, new File( packDir, "${archiveName}.zip"    ))
            fileBean.pack( unpackDir, new File( packDir, "${archiveName}.tar.gz" ))

            fileBean.unpack( new File( packDir, "${archiveName}.jar" ),    jarDir   )
            fileBean.unpack( new File( packDir, "${archiveName}.tar" ),    tarDir   )
            fileBean.unpack( new File( packDir, "${archiveName}.tgz" ),    tgzDir   )
            fileBean.unpack( new File( packDir, "${archiveName}.zip" ),    zipDir   )
            fileBean.unpack( new File( packDir, "${archiveName}.tar.gz" ), tarGzDir )

            verifyBean.equal( unpackDir, jarDir )
            verifyBean.equal( jarDir,    tarDir )
            verifyBean.equal( tarDir,    tgzDir )
            verifyBean.equal( tgzDir,    zipDir )
            verifyBean.equal( zipDir,    tarGzDir )
            verifyBean.equal( tarGzDir,  unpackDir )

            assert unpackDir.directorySize() == testArchives[ archiveName ]
            assert jarDir.directorySize()    == testArchives[ archiveName ]
            assert tarDir.directorySize()    == testArchives[ archiveName ]
            assert tgzDir.directorySize()    == testArchives[ archiveName ]
            assert zipDir.directorySize()    == testArchives[ archiveName ]
            assert tarGzDir.directorySize()  == testArchives[ archiveName ]
        }
    }


    @Test
    void shouldPackWithUpdate()
    {
        def resourcesDir  = new File( 'src/test/resources' )
        def filesToUpdate = [ 'image-3-abc.sima', 'image-3-abc.sima1', 'image-3-abc.zip' ]

        def c =
        {
            File unpackDir, File archive ->

            verifyBean.directory( unpackDir )
            verifyBean.file( archive )

            fileBean.pack( resourcesDir, archive, filesToUpdate, null, true, true, true ) /* Updating an archive */
            fileBean.unpackZipEntries( archive, unpackDir, filesToUpdate )
            fileBean.unpackZipEntries( archive, unpackDir, ['**/*.jar' ] )

            filesToUpdate.each{ String fileName -> verifyBean.file( new File( unpackDir, fileName )) }
            assert unpackDir.listFiles().any{ it.name.endsWith( '.jar' ) }
        }

        for ( archiveName in testArchives().keySet())
        {
            def packDir   = testDir( 'pack' )
            def unpackZip = testDir( 'unpackZip' )
            def unpackJar = testDir( 'unpackJar' )

            c( unpackZip, fileBean.copy( testResource( "${archiveName}.zip" ), packDir ))
            c( unpackJar, fileBean.copy( testResource( "${archiveName}.jar" ), packDir ))

            verifyBean.equal( unpackZip, unpackJar )
            verifyBean.equal( unpackZip, unpackJar, true, '*.jar'   )
            verifyBean.equal( unpackZip, unpackJar, true, '*.sima'  )
            verifyBean.equal( unpackZip, unpackJar, true, '*.sima1' )

            for ( extension in [ 'tar', 'tgz', 'tar.gz' ] )
            {
                shouldFailWith( RuntimeException )
                {
                    fileBean.pack( resourcesDir,
                                   fileBean.copy( new File( resourcesDir, "${archiveName}.$extension" ), packDir ),
                                   filesToUpdate, null, true, true, true )
                }
            }
        }
    }


    @Test
    void shouldCopy()
    {
        def filesToCopy  = [ 'image-3-abc.sima', 'image-3-abc.sima1', 'image-3-abc.zip', 'apache-maven-3.0.1.jar' ]
        def testDir1     = testDir( 'copy-1' )
        def testDir2     = testDir( 'copy-2' )
        def testDir3     = testDir( 'copy-3' )

        for ( fileName in filesToCopy )
        {
            fileBean.copy( testResource( fileName ), testDir1 )
            fileBean.copy( testResource( fileName ), testDir2, fileName )
            fileBean.copy( testResource( fileName ), testDir3, fileName + '-3' )
        }

        verifyBean.equal( testDir1, testDir2 )
        shouldFailAssert { verifyBean.equal( testDir1, testDir3 )}
        shouldFailAssert { verifyBean.equal( testDir2, testDir3 )}
        
        verifyBean.file( filesToCopy.collect { new File( testDir1, it )} as File[] )
        verifyBean.file( filesToCopy.collect { new File( testDir2, it )} as File[] )
        verifyBean.file( filesToCopy.collect { new File( testDir3, it + '-3' )} as File[] )

        assert testDir1.directorySize() == testDir2.directorySize()
        assert testDir1.directorySize() == testDir3.directorySize()
        assert testDir2.directorySize() == testDir3.directorySize()
    }



    @Test
    void shouldUnpack()
    {
        Map testArchives = testArchives()
        def imageDirZip  = testDir( 'image-3-abc-zip'  )
        def imageDirSima = testDir( 'image-3-abc-sima' )
        
        fileBean.unpack( testResource( 'image-3-abc.zip'  ),  imageDirZip )
        fileBean.unpack( testResource( 'image-3-abc.sima' ), imageDirSima )
        assert new File( imageDirZip, '1.png' ).size() == 187933
        verifyBean.equal( imageDirZip, imageDirSima )

        def errorMessage = shouldFailWithCause( IllegalArgumentException )
        {
            fileBean.unpack( testResource( 'image-3-abc.sima1' ), imageDirSima )
        }
        assert errorMessage == '"sima1" (no archive driver installed for these suffixes)'

        for ( archiveName in testArchives.keySet())
        {
            def jarDir       = testDir( 'jar'   )
            def tarDir       = testDir( 'tar'   )
            def tgzDir       = testDir( 'tgz'   )
            def zipDir       = testDir( 'zip'   )
            def tarGzDir     = testDir( 'targz' )

            fileBean.unpack( testResource( "${archiveName}.jar" ),    jarDir   )
            fileBean.unpack( testResource( "${archiveName}.tar" ),    tarDir   )
            fileBean.unpack( testResource( "${archiveName}.tgz" ),    tgzDir   )
            fileBean.unpack( testResource( "${archiveName}.zip" ),    zipDir   )
            fileBean.unpack( testResource( "${archiveName}.tar.gz" ), tarGzDir )

            verifyBean.equal( jarDir,   tarDir )
            verifyBean.equal( tarDir,   tgzDir )
            verifyBean.equal( tgzDir,   zipDir )
            verifyBean.equal( zipDir,   tarGzDir )
            verifyBean.equal( tarGzDir, jarDir )

            assert jarDir.directorySize()   == testArchives[ archiveName ]
            assert tarDir.directorySize()   == testArchives[ archiveName ]
            assert tgzDir.directorySize()   == testArchives[ archiveName ]
            assert zipDir.directorySize()   == testArchives[ archiveName ]
            assert tarGzDir.directorySize() == testArchives[ archiveName ]
        }
    }


    @Test
    void shouldUnpackZipEntries()
    {
        def  mavenZip     = testResource( 'apache-maven-3.0.1.zip' )
        def  mavenJar     = testResource( 'apache-maven-3.0.1.jar' )
        def  mavenTar     = testResource( 'apache-maven-3.0.1.tar' )
        def  mavenTgz     = testResource( 'apache-maven-3.0.1.tgz' )
        def  mavenTarGz   = testResource( 'apache-maven-3.0.1.tar.gz' )
        def  plexusJar    = testResource( 'plexus-component-annotations-1.5.5.jar' )
        List archives     = testArchives().keySet().collect { it + '.zip' }
        def  mavenDir1    = testDir( 'apache-maven-1'  )
        def  mavenDir2    = testDir( 'apache-maven-2'  )
        def  mavenDir3    = testDir( 'apache-maven-3'  )
        def  mavenDir4    = testDir( 'apache-maven-4'  )
        def  mavenDir5    = testDir( 'apache-maven-5'  )
        def  mavenDir6    = testDir( 'apache-maven-6'  )
        def  mavenDir7    = testDir( 'apache-maven-7'  )
        def  mavenDir8    = testDir( 'apache-maven-8'  )
        def  mavenDir9    = testDir( 'apache-maven-9'  )

        def entries      = [ 'apache-maven-3.0.1\\lib\\aether-api-1.8.jar',
                             'apache-maven-3.0.1/lib/commons-cli-1.2.jar',
                             '/apache-maven-3.0.1\\bin\\m2.conf',
                             '/apache-maven-3.0.1/bin/mvn',
                             'apache-maven-3.0.1\\lib\\nekohtml-1.9.6.2.jar',
                             'apache-maven-3.0.1/NOTICE.txt',
                             '/apache-maven-3.0.1/NOTICE.txt',
                             'apache-maven-3.0.1\\NOTICE.txt' ]

        def entries2     = [ 'org/codehaus/plexus/component/annotations/Component.class',
                             'org/codehaus/plexus/component/annotations/Configuration.class',
                             'META-INF/MANIFEST.MF',
                             'META-INF/maven/org.codehaus.plexus/plexus-component-annotations/pom.properties',
                             'META-INF/maven/org.codehaus.plexus/plexus-component-annotations/pom.xml',
                             'org/codehaus/plexus/component/annotations/Requirement.class' ]

        fileBean.unpackZipEntries( mavenZip,  mavenDir1, entries )
        fileBean.unpackZipEntries( mavenZip,  mavenDir2, entries, false )
        fileBean.unpackZipEntries( mavenZip,  mavenDir3, entries, true )
        fileBean.unpackZipEntries( mavenJar,  mavenDir4, entries )
        fileBean.unpackZipEntries( mavenJar,  mavenDir5, entries, true )
        fileBean.unpack( plexusJar, mavenDir6 )
        fileBean.unpackZipEntries( plexusJar, mavenDir7, entries2, true )

        archives.each {
            def testArchiveFile = testResource( it )
            fileBean.unpack( testArchiveFile,  mavenDir8 )
            fileBean.unpackZipEntries( testArchiveFile,  mavenDir9, new ZipFile( testArchiveFile ).entries*.name, true )
        }

        assert mavenDir1.list().size() == 6
        assert mavenDir2.list().size() == 6
        assert mavenDir4.list().size() == 6

        assert mavenDir3.list().size() == 1
        assert mavenDir5.list().size() == 1

        assert mavenDir6.list().size() == 2
        assert mavenDir7.list().size() == 2

        verifyBean.equal( mavenDir1, mavenDir2 )
        verifyBean.equal( mavenDir2, mavenDir4 )
        verifyBean.equal( mavenDir4, mavenDir1 )
        verifyBean.equal( mavenDir3, mavenDir5 )
        verifyBean.equal( mavenDir6, mavenDir7 )
        verifyBean.equal( mavenDir6, mavenDir7 )
        verifyBean.equal( mavenDir8, mavenDir9 )

        assert mavenDir1.directorySize() == 235902
        assert mavenDir2.directorySize() == 235902
        assert mavenDir3.directorySize() == 235902
        assert mavenDir4.directorySize() == 235902
        assert mavenDir5.directorySize() == 235902
        assert mavenDir6.directorySize() == 3420
        assert mavenDir7.directorySize() == 3420
        assert mavenDir8.directorySize() == testArchives().values().sum()
        assert mavenDir9.directorySize() == testArchives().values().sum()

        verifyBean.file( new File( mavenDir1, 'aether-api-1.8.jar' ),
                         new File( mavenDir1, 'commons-cli-1.2.jar' ),
                         new File( mavenDir1, 'm2.conf' ),
                         new File( mavenDir1, 'mvn' ),
                         new File( mavenDir1, 'nekohtml-1.9.6.2.jar' ),
                         new File( mavenDir1, 'NOTICE.txt' ))

        verifyBean.file( new File( mavenDir2, 'aether-api-1.8.jar' ),
                         new File( mavenDir2, 'commons-cli-1.2.jar' ),
                         new File( mavenDir2, 'm2.conf' ),
                         new File( mavenDir2, 'mvn' ),
                         new File( mavenDir2, 'nekohtml-1.9.6.2.jar' ),
                         new File( mavenDir2, 'NOTICE.txt' ))

        verifyBean.file( new File( mavenDir3, 'apache-maven-3.0.1/lib/aether-api-1.8.jar' ),
                         new File( mavenDir3, 'apache-maven-3.0.1/lib/commons-cli-1.2.jar' ),
                         new File( mavenDir3, 'apache-maven-3.0.1/bin/m2.conf' ),
                         new File( mavenDir3, 'apache-maven-3.0.1/bin/mvn' ),
                         new File( mavenDir3, 'apache-maven-3.0.1/lib/nekohtml-1.9.6.2.jar' ),
                         new File( mavenDir3, 'apache-maven-3.0.1/NOTICE.txt' ))

        // Entries that don't exist
        shouldFailWithCause( AssertionError ) { fileBean.unpackZipEntries( plexusJar,  mavenDir7, entries, true )}
        shouldFailWithCause( AssertionError ) { fileBean.unpackZipEntries( plexusJar,  mavenDir7, [ 'org/codehaus/plexus/component'  ], true )}
        shouldFailWithCause( AssertionError ) { fileBean.unpackZipEntries( plexusJar,  mavenDir7, [ '/org/codehaus/plexus/component' ], true )}
        shouldFailWithCause( AssertionError ) { fileBean.unpackZipEntries( plexusJar,  mavenDir7, [ 'META-INF' ], true )}
        shouldFailWithCause( AssertionError ) { fileBean.unpackZipEntries( plexusJar,  mavenDir7, [ '/META-INF' ], true )}
        shouldFailWithCause( AssertionError ) { fileBean.unpackZipEntries( mavenZip,   mavenDir1, [ 'doesnt-exist/entry' ] )}
        shouldFailWithCause( AssertionError ) { fileBean.unpackZipEntries( mavenZip,   mavenDir1, [ '/doesnt-exist/entry' ] )}

        // Not Zip files
        shouldFailAssert { fileBean.unpackZipEntries( mavenTar, mavenDir1, entries )}
        shouldFailAssert { fileBean.unpackZipEntries( mavenTgz,   mavenDir1, entries )}
        shouldFailAssert { fileBean.unpackZipEntries( mavenTarGz, mavenDir1, entries )}

        // Empty list of entries
        shouldFailAssert { fileBean.unpackZipEntries( mavenZip,   mavenDir1, [ null ] )}
        shouldFailAssert { fileBean.unpackZipEntries( mavenZip,   mavenDir1, [ ' ', '',  '  ', null ] )}
        shouldFailAssert { fileBean.unpackZipEntries( mavenZip,   mavenDir1, [ '' ] )}

        // File that doesn't exist
        shouldFailAssert { fileBean.unpackZipEntries( new File( 'doesnt-exist.file' ), mavenDir1, entries )}

        // Should execute normally and not fail
        shouldFailAssert { shouldFailWith( RuntimeException ) { fileBean.unpackZipEntries( plexusJar,  mavenDir7, [ '/org/codehaus/plexus/component/'], true )}}
        shouldFailAssert { shouldFailWith( RuntimeException ) { fileBean.unpackZipEntries( plexusJar,  mavenDir7, [ 'org/codehaus/plexus/component/' ], true )}}
        shouldFailAssert { shouldFailWith( RuntimeException ) { fileBean.unpackZipEntries( plexusJar,  mavenDir7, [ '/META-INF/' ], true )}}
        shouldFailAssert { shouldFailWith( RuntimeException ) { fileBean.unpackZipEntries( plexusJar,  mavenDir7, [ 'META-INF/' ], true )}}
        shouldFailAssert { shouldFailWith( RuntimeException ) { fileBean.unpackZipEntries( plexusJar,  mavenDir7, entries2, true ) }}
    }


    @Test
    void shouldUnpackZipEntriesWithPattern()
    {
        def mavenZip   = testResource( 'apache-maven-3.0.1.zip' )
        def mavenJar   = testResource( 'apache-maven-3.0.1.jar' )
        def mavenDir1  = testDir( 'apache-maven-1' )
        def mavenDir2  = testDir( 'apache-maven-2' )
        def mavenDir3  = testDir( 'apache-maven-3' )
        def mavenDir4  = testDir( 'apache-maven-4' )
        def mavenDir5  = testDir( 'apache-maven-5' )
        def mavenDir6  = testDir( 'apache-maven-6' )
        def mavenDir7  = testDir( 'apache-maven-7' )
        def mavenDir8  = testDir( 'apache-maven-8' )
        def mavenDir9  = testDir( 'apache-maven-9' )
        def mavenDir10 = testDir( 'apache-maven-10' )

        fileBean.unpackZipEntries( mavenZip, mavenDir1,  [ 'apache-maven-3.0.1/**/*.jar' ], true  )
        fileBean.unpackZipEntries( mavenJar, mavenDir2,  [ '**/*.jar' ], true  )
        fileBean.unpackZipEntries( mavenZip, mavenDir3,  [ 'apache-maven-3.0.1/**/*.jar' ], false )
        fileBean.unpackZipEntries( mavenJar, mavenDir4,  [ '**/*.jar' ], false )
        fileBean.unpackZipEntries( mavenZip, mavenDir5,  [ '**/*.xml', '**/conf/**' ], false )
        fileBean.unpackZipEntries( mavenJar, mavenDir6,  [ 'apache-maven-3.0.1/conf/settings.xml', '**/*.xml' ], false )
        fileBean.unpack( mavenZip, mavenDir7 )
        fileBean.unpackZipEntries( mavenJar, mavenDir8,  [ '**' ], true )
        fileBean.unpackZipEntries( mavenJar, mavenDir9,  [ 'apache-maven-3.0.?/**' ], true )
        fileBean.unpackZipEntries( mavenJar, mavenDir10, [ 'apache-maven-?.?.?/**' ], true )

        verifyBean.equal( mavenDir1,  mavenDir2 )
        verifyBean.equal( mavenDir3,  mavenDir4 )
        verifyBean.equal( mavenDir5,  mavenDir6 )
        verifyBean.equal( mavenDir7,  mavenDir8 )
        verifyBean.equal( mavenDir8,  mavenDir9 )
        verifyBean.equal( mavenDir9,  mavenDir10 )
        verifyBean.equal( mavenDir10, mavenDir7 )

        assert mavenDir1.directorySize() == 3301021
        assert mavenDir2.directorySize() == 3301021
        assert mavenDir3.directorySize() == 3301021
        assert mavenDir4.directorySize() == 3301021
        assert mavenDir5.directorySize() == 1704
        assert mavenDir6.directorySize() == 1704
        assert mavenDir7.directorySize() == 3344327
        assert mavenDir8.directorySize() == 3344327

        assert mavenDir1.list().size() == 1
        assert mavenDir2.list().size() == 1
        assert mavenDir3.list().size() == 32
        assert mavenDir4.list().size() == 32
        assert mavenDir5.list().size() == 1
        assert mavenDir6.list().size() == 1
        assert mavenDir7.list().size() == 1
        assert mavenDir8.list().size() == 1


        shouldFailWithCause( AssertionError ) {
            fileBean.unpackZipEntries( mavenZip, mavenDir8, [ '**/*.no-such-file' ], true ) }
        shouldFailWithCause( AssertionError ) {
            fileBean.unpackZipEntries( mavenZip, mavenDir8, [ 'no-such-file', '**/*.no-such-file' ], true ) }
        shouldFailWithCause( AssertionError ) {
            fileBean.unpackZipEntries( mavenZip, mavenDir8, [ '**/*.jar', '**/*.ppt' ], true ) }
        shouldFailWithCause( AssertionError ) {
            fileBean.unpackZipEntries( mavenZip, mavenDir8, [ '**/*.jar', 'no-such-file' ], true ) }
        shouldFailWithCause( AssertionError ) {
            fileBean.unpackZipEntries( mavenZip, mavenDir8, [ '**/*.exe', 'apache-maven-3.0.1/conf/**', ], true ) }
        shouldFailWithCause( AssertionError ) {
            fileBean.unpackZipEntries( mavenZip, mavenDir8, [ '**/*.xml', 'apache-maven-3.3.1/**', ], true ) }
        shouldFailWithCause( AssertionError ) {
            fileBean.unpackZipEntries( mavenZip, mavenDir8, [ '**/*.xml', 'apache-maven-3.3.1', ], true ) }
    }


    @Test
    void shouldMatchType()
    {
        def testFile = new File( testDir( 'typeMatch' ), '111' )
        testFile.write( System.currentTimeMillis() as String )

        assert   fileBean.typeMatch( FileType.ANY,         constantsBean.USER_DIR_FILE )
        assert   fileBean.typeMatch( FileType.DIRECTORIES, constantsBean.USER_DIR_FILE )
        assert ! fileBean.typeMatch( FileType.FILES,       constantsBean.USER_DIR_FILE )

        assert   fileBean.typeMatch( FileType.ANY,         constantsBean.USER_HOME_FILE )
        assert   fileBean.typeMatch( FileType.DIRECTORIES, constantsBean.USER_HOME_FILE )
        assert ! fileBean.typeMatch( FileType.FILES,       constantsBean.USER_HOME_FILE )

        assert   fileBean.typeMatch( FileType.ANY,         testFile )
        assert ! fileBean.typeMatch( FileType.DIRECTORIES, testFile )
        assert   fileBean.typeMatch( FileType.FILES,       testFile )
    }

}
