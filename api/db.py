from elixir import *

metadata.bind = 'sqlite:///posts.sqlite'
metadata.bind.echo = True

class Post(Entity):
    using_options(tablename='posts')

    title = Field(Unicode(255))
    content = Field(UnicodeText)
    author = Field(Unicode(255))
    author_link = Field(Unicode(255))
    issue = ManyToOne('Issue')

class Issue(Entity):
    using_options(tablename='issues')

    date = Field(DateTime)
    posts = OneToMany('Post')
