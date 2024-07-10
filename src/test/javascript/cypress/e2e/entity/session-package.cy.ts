import {
  entityTableSelector,
  entityDetailsButtonSelector,
  entityDetailsBackButtonSelector,
  entityCreateButtonSelector,
  entityCreateSaveButtonSelector,
  entityCreateCancelButtonSelector,
  entityEditButtonSelector,
  entityDeleteButtonSelector,
  entityConfirmDeleteButtonSelector,
} from '../../support/entity';

describe('SessionPackage e2e test', () => {
  const sessionPackagePageUrl = '/session-package';
  const sessionPackagePageUrlPattern = new RegExp('/session-package(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const sessionPackageSample = { name: 'who yesterday worth' };

  let sessionPackage;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/session-packages+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/session-packages').as('postEntityRequest');
    cy.intercept('DELETE', '/api/session-packages/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (sessionPackage) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/session-packages/${sessionPackage.id}`,
      }).then(() => {
        sessionPackage = undefined;
      });
    }
  });

  it('SessionPackages menu should load SessionPackages page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('session-package');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('SessionPackage').should('exist');
    cy.url().should('match', sessionPackagePageUrlPattern);
  });

  describe('SessionPackage page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(sessionPackagePageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create SessionPackage page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/session-package/new$'));
        cy.getEntityCreateUpdateHeading('SessionPackage');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(200);
        });
        cy.url().should('match', sessionPackagePageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/session-packages',
          body: sessionPackageSample,
        }).then(({ body }) => {
          sessionPackage = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/session-packages+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [sessionPackage],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(sessionPackagePageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details SessionPackage page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('sessionPackage');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(200);
        });
        cy.url().should('match', sessionPackagePageUrlPattern);
      });

      it('edit button click should load edit SessionPackage page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('SessionPackage');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(200);
        });
        cy.url().should('match', sessionPackagePageUrlPattern);
      });

      it('edit button click should load edit SessionPackage page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('SessionPackage');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(200);
        });
        cy.url().should('match', sessionPackagePageUrlPattern);
      });

      it('last delete button click should delete instance of SessionPackage', () => {
        cy.get(entityDeleteButtonSelector).last().click();
        cy.getEntityDeleteDialogHeading('sessionPackage').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(200);
        });
        cy.url().should('match', sessionPackagePageUrlPattern);

        sessionPackage = undefined;
      });
    });
  });

  describe('new SessionPackage page', () => {
    beforeEach(() => {
      cy.visit(`${sessionPackagePageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('SessionPackage');
    });

    it('should create an instance of SessionPackage', () => {
      cy.get(`[data-cy="name"]`).type('er against plastic');
      cy.get(`[data-cy="name"]`).should('have.value', 'er against plastic');

      cy.get(`[data-cy="price"]`).type('7486');
      cy.get(`[data-cy="price"]`).should('have.value', '7486');

      cy.get(`[data-cy="credits"]`).type('10888');
      cy.get(`[data-cy="credits"]`).should('have.value', '10888');

      cy.get(`[data-cy="startDate"]`).type('2023-11-16T01:58');
      cy.get(`[data-cy="startDate"]`).blur();
      cy.get(`[data-cy="startDate"]`).should('have.value', '2023-11-16T01:58');

      cy.get(`[data-cy="endDate"]`).type('2023-11-16T07:05');
      cy.get(`[data-cy="endDate"]`).blur();
      cy.get(`[data-cy="endDate"]`).should('have.value', '2023-11-16T07:05');

      cy.get(`[data-cy="reviseCount"]`).type('10700');
      cy.get(`[data-cy="reviseCount"]`).should('have.value', '10700');

      cy.get(`[data-cy="cancelCount"]`).type('29743');
      cy.get(`[data-cy="cancelCount"]`).should('have.value', '29743');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response.statusCode).to.equal(201);
        sessionPackage = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response.statusCode).to.equal(200);
      });
      cy.url().should('match', sessionPackagePageUrlPattern);
    });
  });
});
