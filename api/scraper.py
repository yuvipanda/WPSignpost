from lxml import html, etree
from datetime import datetime
from dateutil import parser
from mwapi import MWApi
import codecs
import requests
import urllib2
import re

from flask import Flask

from db import *

START_YEAR = 2005
CUR_YEAR = datetime.now().year


api = MWApi('http://en.wikipedia.org')

def content_for_title(title):
    title = urllib2.unquote(title)
    data = api.get({'action': 'parse', 'page': title, 'redirects': '1', 'prop': 'text'})['parse']
    text = data['text']['*']
    return text

def get_subpages(prefix, namespace):
    pages = api.get({'action': 'query', 'list': 'allpages', 'apprefix': prefix, 'apnamespace': namespace, 'aplimit': 500})['query']['allpages']
    return [page['title'] for page in pages]

def title_for_year(year):
    return 'Wikipedia:Wikipedia_Signpost/Archives/' + str(year)

def drop_child_elements(element, selectors):
    drop_tags = element.cssselect(', '.join(selectors))
    if len(drop_tags) != 0:
        for t in drop_tags:
            t.drop_tree()

DROP_ARCHIVE_SELECTORS = ['.hlist', '.signpost-article', 'table']
DROP_PAGE_SELECTORS = ['.floatright', 'center', '.NavFrame', '.signpost-sidebar', '.signpost-comments', '.signpost-byline']

EXCLUDE_PAGE_TAGS = []

def parse_image(doc):
    link = doc.cssselect("a[href*='/wiki/File:']")
    if link:
        name = link[0].get('href').replace('/wiki/', '')
        name = urllib2.unquote(name)
        data = api.get({'action': 'query', 'prop': 'imageinfo', 'iiprop': 'url', 'iiurlwidth': 512, 'iiurlheight': 512, 'titles': name})['query']['pages'].values()[0]
        if 'imageinfo' not in data:
            return None
        return data['imageinfo'][0]['thumburl']
    else:
        return None

def parse_article(title):
    doc = html.document_fromstring(content_for_title(title))
    # Author tag
    author_el = doc.cssselect("dd a[href*='User:'], .signpost-author a[href*='User:']")
    author_el = author_el[0] if len(author_el) != 0 else None
    if author_el is not None:
        author_name, author_link = unicode(author_el.text_content()), unicode(author_el.get('href'))
        author_parent = author_el.xpath("ancestor::dl")
        if author_parent:
            author_parent[0].drop_tree()
    else:
        author_name = author_link = u'Unknown'

    drop_child_elements(doc, DROP_PAGE_SELECTORS)
    image = parse_image(doc)
    title = doc.cssselect('h2')[0]
    el = title.getnext()
    contents = u''
    while el is not None and el.tag is not etree.Comment:
        if el.tag not in EXCLUDE_PAGE_TAGS:
            contents += html.tostring(el, encoding=unicode)
        el = el.getnext()
    title_string = title.text_content()
    return (title_string, author_name, author_link, contents, image)

def save_issue(date_string):
    date = parser.parse(date_string)
    if Issue.query.filter_by(date=date).count() != 0:
        print "Skipping %s" % date
        return None
    cur_issue = Issue(date=date, permalink="en.wikipedia.org/wiki/Wikipedia:Wikipedia_Signpost/Archives/" + date.strftime("%Y-%m-%d"))
    db.session.add(cur_issue)

    issue = "Wikipedia:Wikipedia Signpost/Archives/" + date.strftime("%Y-%m-%d")
    doc = html.document_fromstring(content_for_title(issue))
    article_elements = doc.cssselect("li a, .signpost-archive a")
    articles = [article.get("href").replace("/wiki/", "") for article in article_elements]
    for article in articles:
        page_title = article
        permalink = "en.wikipedia.org/wiki/" + page_title
        try:
            title, author_name, author_link, content, image_link = parse_article(page_title)
        except Exception as e:
            if e.message == "Redirects found! KITTENS KILLED!":
                continue
            else:
                print permalink
                raise
        post = Post(permalink=permalink, title=title, content=content, author=author_name, author_link=author_link, issue=cur_issue, image_link=image_link)
        db.session.add(post)
    db.session.commit()
    return cur_issue

if __name__ == "__main__":
    setup_script()
    db.create_all()

    issues = [issue for issue in get_subpages("Wikipedia_Signpost/Archives/", 4) if '-' in issue]
    for issue in issues:
        print issue
        date = issue.split('/')[-1]
        print save_issue(date)
