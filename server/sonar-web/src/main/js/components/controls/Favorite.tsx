/*
 * SonarQube
 * Copyright (C) 2009-2022 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import classNames from 'classnames';
import * as React from 'react';
import { addFavorite, removeFavorite } from '../../api/favorites';
import { translate } from '../../helpers/l10n';
import FavoriteIcon from '../icons/FavoriteIcon';
import { ButtonLink } from './buttons';
import Tooltip from './Tooltip';

interface Props {
  className?: string;
  component: string;
  favorite: boolean;
  qualifier: string;
  handleFavorite?: (component: string, isFavorite: boolean) => void;
}

interface State {
  favorite: boolean;
}

export default class Favorite extends React.PureComponent<Props, State> {
  mounted = false;
  buttonNode?: HTMLElement | null;

  constructor(props: Props) {
    super(props);

    this.state = {
      favorite: props.favorite
    };
  }

  componentDidMount() {
    this.mounted = true;
  }

  componentDidUpdate(_prevProps: Props, prevState: State) {
    if (prevState.favorite !== this.props.favorite) {
      this.setState({ favorite: this.props.favorite });
    }
  }

  componentWillUnmount() {
    this.mounted = false;
  }

  toggleFavorite = () => {
    const newFavorite = !this.state.favorite;
    const apiMethod = newFavorite ? addFavorite : removeFavorite;

    return apiMethod(this.props.component).then(() => {
      if (this.mounted) {
        this.setState({ favorite: newFavorite }, () => {
          if (this.props.handleFavorite) {
            this.props.handleFavorite(this.props.component, newFavorite);
          }
          if (this.buttonNode) {
            this.buttonNode.focus();
          }
        });
      }
    });
  };

  render() {
    const { className, qualifier } = this.props;
    const { favorite } = this.state;

    const tooltip = favorite
      ? translate('favorite.current', qualifier)
      : translate('favorite.check', qualifier);
    const ariaLabel = translate('favorite.action', favorite ? 'remove' : 'add');

    return (
      <Tooltip overlay={tooltip}>
        <ButtonLink
          aria-label={ariaLabel}
          innerRef={node => (this.buttonNode = node)}
          className={classNames('favorite-link', 'link-no-underline', className)}
          onClick={this.toggleFavorite}>
          <FavoriteIcon favorite={favorite} />
        </ButtonLink>
      </Tooltip>
    );
  }
}
